#!/usr/bin/env python3
"""
Seed Data Script for BirdBook Microservices
Waits for all services to be ready, then populates data via API Gateway
"""

import requests
import time
import sys
from typing import Dict, List

API_GATEWAY = "http://localhost:8080"
MAX_RETRIES = 30
RETRY_DELAY = 2

class Color:
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BLUE = '\033[94m'
    END = '\033[0m'

def log(message: str, color: str = Color.BLUE):
    print(f"{color}[SEED] {message}{Color.END}")

def wait_for_services():
    """Wait for all services to be healthy"""
    services = {
        "Eureka": "http://localhost:8761/actuator/health",
        "API Gateway": "http://localhost:8080/actuator/health",
        "User Service": "http://localhost:8081/actuator/health",
        "Bird Service": "http://localhost:8082/actuator/health",
        "Post Service": "http://localhost:8083/actuator/health",
        "Group Service": "http://localhost:8084/actuator/health"
    }
    
    log("Waiting for services to be ready...")
    
    for service_name, url in services.items():
        for attempt in range(MAX_RETRIES):
            try:
                response = requests.get(url, timeout=2)
                if response.status_code == 200:
                    log(f"✓ {service_name} is ready", Color.GREEN)
                    break
            except requests.exceptions.RequestException:
                if attempt < MAX_RETRIES - 1:
                    time.sleep(RETRY_DELAY)
                else:
                    log(f"✗ {service_name} failed to start", Color.RED)
                    sys.exit(1)
    
    log("All services are ready!", Color.GREEN)
    time.sleep(2)  # Extra buffer

def create_admin_user() -> tuple[str, str]:
    """Create the first admin user (public endpoint) and return (userId, token)"""
    log("Creating initial admin user...")
    
    admin_data = {"username": "admin_alice", "password": "Admin1!"}
    
    try:
        # Register admin
        response = requests.post(
            f"{API_GATEWAY}/users",
            json=admin_data,
            timeout=5
        )
        
        if response.status_code in [200, 201]:
            log("  ✓ Admin user registered", Color.GREEN)
        elif response.status_code == 409:
            log("  ℹ Admin user already exists", Color.YELLOW)
        else:
            log(f"  ✗ Failed to register admin: {response.status_code}", Color.RED)
            return None, None
        
        # Login to get token
        log("  Logging in as admin...")
        login_response = requests.post(
            f"{API_GATEWAY}/auth/login",
            json=admin_data,
            timeout=5
        )
        
        if login_response.status_code == 200:
            result = login_response.json()
            token = result.get('token') or result.get('accessToken')
            user_id = result.get('userId') or result.get('id')
            
            if token:
                log("  ✓ Got authentication token", Color.GREEN)
                return user_id, token
            else:
                log("  ✗ No token in response", Color.RED)
                return None, None
        else:
            log(f"  ✗ Login failed: {login_response.status_code}", Color.RED)
            return None, None
            
    except Exception as e:
        log(f"  ✗ Error creating admin: {str(e)}", Color.RED)
        return None, None

def seed_users(token: str) -> Dict[str, str]:
    """Create additional users with auth token"""
    log("Creating additional users...")
    
    headers = {"Authorization": f"Bearer {token}"}
    
    users = [
        {"username": "super_sam", "password": "Super1!", "role": "SUPER_USER"},
        {"username": "rockPigeonLover41", "password": "Bird1!", "role": "BASIC_USER"},
        {"username": "taylor_b", "password": "Bird1!", "role": "BASIC_USER"},
        {"username": "jordanlee2", "password": "Bird1!", "role": "BASIC_USER"},
        {"username": "camila_ro", "password": "Bird1!", "role": "BASIC_USER"},
        {"username": "owl_at_dawn", "password": "Bird1!", "role": "BASIC_USER"},
    ]
    
    user_map = {"admin_alice": "admin_id"}  # Add admin to map
    
    for user_data in users:
        try:
            response = requests.post(
                f"{API_GATEWAY}/users",
                json=user_data,
                headers=headers,
                timeout=5
            )
            if response.status_code in [200, 201]:
                result = response.json() if response.text else {}
                user_id = result.get('id', 'generated_id')
                user_map[user_data['username']] = user_id
                log(f"  ✓ Created user: {user_data['username']}", Color.GREEN)
            else:
                log(f"  ✗ Failed to create {user_data['username']}: {response.status_code}", Color.RED)
        except Exception as e:
            log(f"  ✗ Error creating {user_data['username']}: {str(e)}", Color.RED)
    
    log(f"Created {len(user_map)} users", Color.GREEN)
    return user_map

def seed_birds(token: str) -> Dict[str, str]:
    """Create birds with auth token and return mapping of commonName -> birdId"""
    log("Creating birds...")
    
    headers = {"Authorization": f"Bearer {token}"}
    
    birds = [
        {"commonName": "Mallard", "scientificName": "Anas platyrhynchos"},
        {"commonName": "Bald Eagle", "scientificName": "Haliaeetus leucocephalus"},
        {"commonName": "Great Blue Heron", "scientificName": "Ardea herodias"},
        {"commonName": "Northern Cardinal", "scientificName": "Cardinalis cardinalis"},
        {"commonName": "Blue Jay", "scientificName": "Cyanocitta cristata"},
        {"commonName": "American Robin", "scientificName": "Turdus migratorius"},
        {"commonName": "Rock Pigeon", "scientificName": "Columba livia"},
        {"commonName": "Mourning Dove", "scientificName": "Zenaida macroura"},
        {"commonName": "Red-tailed Hawk", "scientificName": "Buteo jamaicensis"},
        {"commonName": "Barred Owl", "scientificName": "Strix varia"},
    ]
    
    bird_map = {}
    
    for bird_data in birds:
        try:
            # Add Wikimedia image URL
            file_name = bird_data['commonName'].replace(' ', '_').replace("'", "") + '.jpg'
            bird_data['imageURL'] = f"https://commons.wikimedia.org/wiki/Special:FilePath/{file_name}?width=600"
            
            response = requests.post(
                f"{API_GATEWAY}/birds",
                json=bird_data,
                headers=headers,
                timeout=5
            )
            if response.status_code in [200, 201]:
                result = response.json() if response.text else {}
                bird_id = result.get('id', 'generated_id')
                bird_map[bird_data['commonName']] = bird_id
                log(f"  ✓ Created bird: {bird_data['commonName']}", Color.GREEN)
            else:
                log(f"  ✗ Failed to create {bird_data['commonName']}: {response.status_code}", Color.RED)
        except Exception as e:
            log(f"  ✗ Error creating {bird_data['commonName']}: {str(e)}", Color.RED)
    
    log(f"Created {len(bird_map)} birds", Color.GREEN)
    return bird_map

def seed_groups(user_map: Dict[str, str], token: str) -> Dict[str, str]:
    """Create groups with auth token and return mapping of groupName -> groupId"""
    log("Creating groups...")
    
    headers = {"Authorization": f"Bearer {token}"}
    
    groups = [
        {"name": "DFW Birders", "owner": "admin_alice"},
        {"name": "Coastal Bird Committee", "owner": "super_sam"},
        {"name": "Hill Country Spotters", "owner": "admin_alice"},
        {"name": "Gulf Coast Birding", "owner": "super_sam"},
        {"name": "Metro Birdwatchers", "owner": "admin_alice"},
    ]
    
    group_map = {}
    
    for group_data in groups:
        try:
            owner_username = group_data.pop('owner')
            owner_id = user_map.get(owner_username)
            
            if not owner_id:
                log(f"  ✗ Owner {owner_username} not found", Color.RED)
                continue
            
            group_data['ownerId'] = owner_id
            
            response = requests.post(
                f"{API_GATEWAY}/groups",
                json=group_data,
                headers=headers,
                timeout=5
            )
            if response.status_code in [200, 201]:
                result = response.json() if response.text else {}
                group_id = result.get('id', 'generated_id')
                group_map[group_data['name']] = group_id
                log(f"  ✓ Created group: {group_data['name']}", Color.GREEN)
            else:
                log(f"  ✗ Failed to create {group_data['name']}: {response.status_code}", Color.RED)
        except Exception as e:
            log(f"  ✗ Error creating {group_data['name']}: {str(e)}", Color.RED)
    
    log(f"Created {len(group_map)} groups", Color.GREEN)
    return group_map

def seed_posts(user_map: Dict[str, str], bird_map: Dict[str, str], group_map: Dict[str, str], token: str):
    """Create sample posts with auth token"""
    log("Creating posts...")
    
    headers = {"Authorization": f"Bearer {token}"}
    
    posts = [
        {
            "username": "rockPigeonLover41",
            "bird": "Rock Pigeon",
            "group": "DFW Birders",
            "header": "Morning sighting at White Rock Lake",
            "textBody": "Caught a small flock skimming the water just after sunrise. The light was perfect!",
            "latitude": 32.8256,
            "longitude": -96.7166
        },
        {
            "username": "taylor_b",
            "bird": "Bald Eagle",
            "group": "Metro Birdwatchers",
            "header": "Soaring over Trinity River",
            "textBody": "Gliding circles above the treeline for several minutes. Surprised to see it so close to the city.",
            "latitude": 32.8145,
            "longitude": -96.7459
        },
        {
            "username": "jordanlee2",
            "bird": "Great Blue Heron",
            "group": "Coastal Bird Committee",
            "header": "Calm morning at the Aransas boardwalk",
            "textBody": "Calm morning, glassy water. Great views and behavior notes—feeding method was easy to observe.",
            "latitude": 28.0206,
            "longitude": -96.9903
        },
        {
            "username": "camila_ro",
            "bird": "Northern Cardinal",
            "group": "Hill Country Spotters",
            "header": "Backyard visitor",
            "textBody": "Bright red male visited the feeder this morning. Always a beautiful sight!",
            "latitude": 30.2649,
            "longitude": -97.7733
        },
        {
            "username": "owl_at_dawn",
            "bird": "Barred Owl",
            "group": "DFW Birders",
            "header": "Surprise at Brazos Bend",
            "textBody": "Wasn't expecting to see this species here today. Stayed still and it lingered for a good five minutes.",
            "latitude": 29.3928,
            "longitude": -95.6083
        },
    ]
    
    created_count = 0
    
    for post_data in posts:
        try:
            username = post_data.pop('username')
            bird_name = post_data.pop('bird')
            group_name = post_data.pop('group')
            
            user_id = user_map.get(username)
            bird_id = bird_map.get(bird_name)
            group_id = group_map.get(group_name)
            
            if not all([user_id, bird_id, group_id]):
                log(f"  ✗ Missing IDs for post by {username}", Color.RED)
                continue
            
            post_payload = {
                **post_data,
                "userId": user_id,
                "birdId": bird_id,
                "groupId": group_id,
                "tags": {
                    "latitude": post_data.pop('latitude'),
                    "longitude": post_data.pop('longitude')
                }
            }
            
            response = requests.post(
                f"{API_GATEWAY}/posts",
                json=post_payload,
                headers=headers,
                timeout=5
            )
            if response.status_code in [200, 201]:
                created_count += 1
                log(f"  ✓ Created post: {post_payload['header']}", Color.GREEN)
            else:
                log(f"  ✗ Failed to create post: {response.status_code}", Color.RED)
        except Exception as e:
            log(f"  ✗ Error creating post: {str(e)}", Color.RED)
    
    log(f"Created {created_count} posts", Color.GREEN)

def main():
    log("Starting BirdBook data seeding...", Color.YELLOW)
    
    try:
        wait_for_services()
        
        # Step 1: Create admin user and get auth token
        admin_id, token = create_admin_user()
        
        if not token:
            log("✗ Failed to get authentication token. Cannot proceed.", Color.RED)
            sys.exit(1)
        
        # Step 2: Seed data with auth token
        user_map = seed_users(token)
        user_map['admin_alice'] = admin_id  # Add admin to map
        
        bird_map = seed_birds(token)
        group_map = seed_groups(user_map, token)
        seed_posts(user_map, bird_map, group_map, token)
        
        log("✓ Data seeding completed successfully!", Color.GREEN)
        log(f"Created: {len(user_map)} users, {len(bird_map)} birds, {len(group_map)} groups", Color.GREEN)
        
    except KeyboardInterrupt:
        log("\n✗ Seeding interrupted by user", Color.RED)
        sys.exit(1)
    except Exception as e:
        log(f"✗ Seeding failed: {str(e)}", Color.RED)
        sys.exit(1)

if __name__ == "__main__":
    main()