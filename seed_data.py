#!/usr/bin/env python3
"""BirdBook microservices seed script.

- Waits for services
- Creates/logs in users
- Seeds birds/groups/sightings through API gateway
- Uses multipart where controllers require it
- Uploads sample images from backend/images when available
"""

import json
import os
import re
import subprocess
import sys
import time
from pathlib import Path
from typing import Dict, Optional

import requests

API_GATEWAY = os.getenv("API_GATEWAY_URL", "http://localhost:8080")
MAX_RETRIES = 30
RETRY_DELAY = 2
REQUEST_TIMEOUT = 10

BASE_DIR = Path(__file__).resolve().parent
MONOLITH_IMAGES_DIR = BASE_DIR / "backend" / "images"
PROFILE_IMAGES_DIR = MONOLITH_IMAGES_DIR / "profile_pictures"


class Color:
    GREEN = "\033[92m"
    YELLOW = "\033[93m"
    RED = "\033[91m"
    BLUE = "\033[94m"
    END = "\033[0m"


def log(message: str, color: str = Color.BLUE) -> None:
    print(f"{color}[SEED] {message}{Color.END}")


def wait_for_services() -> None:
    services = {
        "Eureka": "http://localhost:8761/actuator/health",
        "API Gateway": f"{API_GATEWAY}/actuator/health",
        "User Service": "http://localhost:8081/actuator/health",
        "Bird Service": "http://localhost:8082/actuator/health",
        "Post Service": "http://localhost:8083/actuator/health",
        "Group Service": "http://localhost:8084/actuator/health",
    }

    log("Waiting for services to be ready...", Color.YELLOW)
    for service_name, url in services.items():
        for attempt in range(MAX_RETRIES):
            try:
                response = requests.get(url, timeout=3)
                if 100 <= response.status_code < 500:
                    log(f"✓ {service_name} is ready", Color.GREEN)
                    break
            except requests.RequestException:
                pass

            if attempt == MAX_RETRIES - 1:
                log(f"✗ {service_name} failed to start", Color.RED)
                sys.exit(1)

            time.sleep(RETRY_DELAY)

    wait_for_gateway_routes()

    log("All services are ready!", Color.GREEN)
    time.sleep(1)


def wait_for_gateway_routes() -> None:
    probes = {
        "user route": f"{API_GATEWAY}/users/__seed_probe__",
        "bird route": f"{API_GATEWAY}/birds/__seed_probe__",
        "post route": f"{API_GATEWAY}/sightings/__seed_probe__",
        "group route": f"{API_GATEWAY}/groups/__seed_probe__",
    }

    log("Waiting for gateway routes to become available...", Color.YELLOW)
    for route_name, url in probes.items():
        for attempt in range(MAX_RETRIES):
            try:
                response = requests.get(url, timeout=3)
                if response.status_code not in (502, 503, 504):
                    log(f"✓ {route_name} is routable", Color.GREEN)
                    break
            except requests.RequestException:
                pass

            if attempt == MAX_RETRIES - 1:
                log(f"✗ {route_name} is not routable yet", Color.RED)
                sys.exit(1)

            time.sleep(RETRY_DELAY)


def normalize_name(value: str) -> str:
    return re.sub(r"[^a-z0-9]", "", value.lower())


def build_image_index(directory: Path) -> Dict[str, Path]:
    index: Dict[str, Path] = {}
    if not directory.exists():
        return index

    for p in directory.iterdir():
        if p.is_file():
            key = normalize_name(p.stem)
            if key and key not in index:
                index[key] = p
    return index


IMAGE_INDEX = build_image_index(MONOLITH_IMAGES_DIR)


def find_bird_image(common_name: str) -> Optional[Path]:
    candidates = [
        common_name,
        common_name.replace(" ", ""),
        common_name.replace("-", ""),
        common_name.replace("'", ""),
    ]
    for candidate in candidates:
        key = normalize_name(candidate)
        if key in IMAGE_INDEX:
            return IMAGE_INDEX[key]
    return None


def first_profile_image() -> Optional[Path]:
    if not PROFILE_IMAGES_DIR.exists():
        return None
    files = [p for p in PROFILE_IMAGES_DIR.iterdir() if p.is_file()]
    return files[0] if files else None


def parse_id_value(raw):
    if isinstance(raw, str):
        return raw
    if isinstance(raw, dict):
        return raw.get("$oid") or raw.get("id")
    return None


def extract_id(payload: dict) -> Optional[str]:
    if not isinstance(payload, dict):
        return None
    for key in ("id", "_id", "userId", "groupId", "birdId", "postId"):
        if key in payload:
            parsed = parse_id_value(payload[key])
            if parsed:
                return parsed
    return None


def safe_json(response: requests.Response) -> dict:
    try:
        return response.json()
    except Exception:
        return {}


def get_user_id_by_username(username: str) -> Optional[str]:
    try:
        response = requests.get(f"{API_GATEWAY}/users", timeout=REQUEST_TIMEOUT)
        if response.status_code != 200:
            return None

        payload = safe_json(response)
        if not isinstance(payload, list):
            return None

        for user in payload:
            if not isinstance(user, dict):
                continue
            if user.get("username") == username:
                return parse_id_value(user.get("id") or user.get("userId") or user.get("_id"))
    except Exception:
        return None
    return None


def get_bird_id_by_common_name(common_name: str) -> Optional[str]:
    try:
        response = requests.get(f"{API_GATEWAY}/birds", timeout=REQUEST_TIMEOUT)
        if response.status_code != 200:
            return None

        payload = safe_json(response)
        if not isinstance(payload, list):
            return None

        for bird in payload:
            if not isinstance(bird, dict):
                continue
            if bird.get("commonName") == common_name:
                return parse_id_value(bird.get("id") or bird.get("birdId") or bird.get("_id"))
    except Exception:
        return None
    return None


def get_group_id_by_name(group_name: str) -> Optional[str]:
    try:
        response = requests.get(f"{API_GATEWAY}/groups", timeout=REQUEST_TIMEOUT)
        if response.status_code != 200:
            return None

        payload = safe_json(response)
        if not isinstance(payload, list):
            return None

        for group in payload:
            if not isinstance(group, dict):
                continue
            if group.get("name") == group_name:
                return parse_id_value(group.get("id") or group.get("groupId") or group.get("_id"))
    except Exception:
        return None
    return None


def existing_sighting_headers() -> set[str]:
    headers: set[str] = set()
    try:
        response = requests.get(f"{API_GATEWAY}/sightings", timeout=REQUEST_TIMEOUT)
        if response.status_code != 200:
            return headers

        payload = safe_json(response)
        if not isinstance(payload, list):
            return headers

        for sighting in payload:
            if not isinstance(sighting, dict):
                continue
            header = sighting.get("header")
            if isinstance(header, str) and header.strip():
                headers.add(header.strip())
    except Exception:
        return headers
    return headers


def signup_user(username: str, password: str) -> tuple[int, Optional[str]]:
    try:
        response = requests.post(
            f"{API_GATEWAY}/auth/signup",
            json={"username": username, "password": password},
            timeout=REQUEST_TIMEOUT,
        )
        user_id = extract_id(safe_json(response))
        return response.status_code, user_id
    except Exception:
        return 0, None


def ensure_user(username: str, password: str) -> Optional[str]:
    signup_status, signup_user_id = signup_user(username, password)
    if signup_status == 201:
        log(f"  ✓ Registered user: {username}", Color.GREEN)
        if signup_user_id:
            return signup_user_id
    elif signup_status == 409:
        log(f"  ℹ User already exists: {username}", Color.YELLOW)
    elif signup_status != 0:
        log(f"  ⚠ Register returned {signup_status} for {username}", Color.YELLOW)

    try:
        login_response = requests.post(
            f"{API_GATEWAY}/auth/login",
            json={"username": username, "password": password},
            timeout=REQUEST_TIMEOUT,
        )
        if login_response.status_code != 200:
            existing_id = get_user_id_by_username(username)
            if existing_id:
                log(
                    f"  ℹ Resolved existing user id for {username} after login {login_response.status_code}",
                    Color.YELLOW,
                )
                return existing_id

            if login_response.status_code == 401:
                for i in range(1, 6):
                    alias_username = f"{username}_seed{i}"
                    alias_status, alias_id = signup_user(alias_username, password)
                    if alias_status == 201 and alias_id:
                        log(
                            f"  ⚠ Username {username} locked; using {alias_username}",
                            Color.YELLOW,
                        )
                        return alias_id

            log(f"  ✗ Login failed for {username}: {login_response.status_code}", Color.RED)
            return None

        response_payload = safe_json(login_response)
        user_id = extract_id(response_payload)
        if not user_id:
            log(f"  ✗ Could not parse user id for {username}", Color.RED)
            return None

        log(f"  ✓ Logged in user: {username}", Color.GREEN)
        return user_id
    except Exception as exc:
        log(f"  ✗ Login exception for {username}: {exc}", Color.RED)
        existing_id = get_user_id_by_username(username)
        if existing_id:
            log(f"  ℹ Resolved existing user id for {username}", Color.YELLOW)
            return existing_id
        return None


def login_session(username: str, password: str) -> Optional[requests.Session]:
    session = requests.Session()
    try:
        response = session.post(
            f"{API_GATEWAY}/auth/login",
            json={"username": username, "password": password},
            timeout=REQUEST_TIMEOUT,
        )
        if response.status_code == 200:
            return session
    except Exception:
        pass
    return None


def get_current_user(session: requests.Session) -> Optional[dict]:
    try:
        response = session.get(f"{API_GATEWAY}/auth/me", timeout=REQUEST_TIMEOUT)
        if response.status_code == 200:
            payload = safe_json(response)
            if isinstance(payload, dict):
                return payload
    except Exception:
        return None
    return None


def bootstrap_roles_via_mongo(desired_roles: Dict[str, str]) -> bool:
    if os.getenv("SEED_BOOTSTRAP_ROLES", "1") != "1":
        return False

    container = os.getenv("USER_MONGO_CONTAINER", "user-mongodb")
    configured_database = os.getenv("USER_MONGO_DB")
    databases_to_try = [
        configured_database,
        "user_db",
        "users_db",
    ]
    databases_to_try = [db for db in databases_to_try if db]

    role_updates = "".join(
        [
            (
                f'db.users.updateOne({{ username: "{username}" }}, '
                f'{{ $set: {{ role: "{role}" }} }});'
            )
            for username, role in desired_roles.items()
        ]
    )

    verify_query = "".join(
        [
            'db.users.find('
            '{ username: { $in: ["admin_alice","super_sam","rockPigeonLover41","taylor_b","jordanlee2","camila_ro","owl_at_dawn"] } }, '
            '{ _id: 0, username: 1, role: 1 }'
            ').toArray();'
        ]
    )

    try:
        for database in databases_to_try:
            command = [
                "docker",
                "exec",
                container,
                "mongosh",
                database,
                "--quiet",
                "--eval",
                role_updates + verify_query,
            ]

            completed = subprocess.run(
                command,
                capture_output=True,
                text=True,
                timeout=20,
                check=False,
            )

            output = (completed.stdout or "") + (completed.stderr or "")
            if completed.returncode != 0:
                continue

            if "super_sam" in output or "admin_alice" in output:
                log(
                    f"  ✓ Role bootstrap applied directly in Mongo ({database})",
                    Color.GREEN,
                )
                return True

        log(
            "  ⚠ Role bootstrap via Mongo could not find seeded users in tried databases: "
            + ", ".join(databases_to_try),
            Color.YELLOW,
        )
        return False
    except Exception as exc:
        log(f"  ⚠ Role bootstrap exception: {exc}", Color.YELLOW)
        return False


def apply_seed_roles(user_map: Dict[str, str]) -> None:
    desired_roles = {
        "admin_alice": "ADMIN_USER",
        "super_sam": "SUPER_USER",
        "rockPigeonLover41": "BASIC_USER",
        "taylor_b": "BASIC_USER",
        "jordanlee2": "BASIC_USER",
        "camila_ro": "BASIC_USER",
        "owl_at_dawn": "BASIC_USER",
    }

    credentials = {
        "admin_alice": "Admin1!",
        "super_sam": "Super1!",
        "rockPigeonLover41": "Bird1!",
        "taylor_b": "Bird1!",
        "jordanlee2": "Bird1!",
        "camila_ro": "Bird1!",
        "owl_at_dawn": "Bird1!",
    }

    log("Applying role assignments...", Color.YELLOW)

    current_roles: Dict[str, str] = {}
    super_session: Optional[requests.Session] = None

    for username, password in credentials.items():
        session = login_session(username, password)
        if not session:
            continue

        me = get_current_user(session)
        if not me:
            continue

        role = me.get("role")
        if isinstance(role, str):
            current_roles[username] = role
            if role == "SUPER_USER" and super_session is None:
                super_session = session

    if super_session is None:
        bootstrapped = bootstrap_roles_via_mongo(desired_roles)
        if bootstrapped:
            current_roles.clear()
            for username, password in credentials.items():
                session = login_session(username, password)
                if not session:
                    continue
                me = get_current_user(session)
                if not me:
                    continue
                role = me.get("role")
                if isinstance(role, str):
                    current_roles[username] = role
                    if role == "SUPER_USER" and super_session is None:
                        super_session = session

    if super_session is None:
        log(
            "  ⚠ No SUPER_USER session available; role updates skipped. "
            "Set SEED_BOOTSTRAP_ROLES=1 and ensure user-mongodb container is running.",
            Color.YELLOW,
        )
        return

    for username, target_role in desired_roles.items():
        user_id = user_map.get(username)
        if not user_id:
            continue

        if current_roles.get(username) == target_role:
            log(f"  ✓ Role already set for {username}: {target_role}", Color.GREEN)
            continue

        try:
            response = super_session.patch(
                f"{API_GATEWAY}/users/{user_id}/role",
                json={"role": target_role},
                timeout=REQUEST_TIMEOUT,
            )
            if response.status_code == 200:
                log(f"  ✓ Role updated for {username}: {target_role}", Color.GREEN)
            else:
                log(
                    f"  ⚠ Failed to set role for {username}: "
                    f"{response.status_code} {response.text[:120]}",
                    Color.YELLOW,
                )
        except Exception as exc:
            log(f"  ⚠ Role update exception for {username}: {exc}", Color.YELLOW)


def complete_onboarding(
    session: requests.Session,
    first_name: str,
    last_name: str,
    location: str,
    profile_photo: Optional[Path],
) -> bool:
    files = {
        "firstName": (None, first_name),
        "lastName": (None, last_name),
        "location": (None, location),
    }

    opened_file = None
    try:
        if profile_photo and profile_photo.exists():
            opened_file = profile_photo.open("rb")
            files["profilePhoto"] = (profile_photo.name, opened_file, "image/jpeg")

        response = session.post(
            f"{API_GATEWAY}/users/onboard",
            files=files,
            timeout=REQUEST_TIMEOUT,
        )
        return response.status_code in (200, 201)
    finally:
        if opened_file:
            opened_file.close()


def post_multipart_json(
    url: str,
    json_part_name: str,
    payload: dict,
    image_path: Optional[Path] = None,
    params: Optional[dict] = None,
) -> requests.Response:
    files = {
        json_part_name: (None, json.dumps(payload), "application/json"),
    }

    opened_file = None
    try:
        if image_path and image_path.exists():
            opened_file = image_path.open("rb")
            files["image"] = (image_path.name, opened_file, "image/jpeg")

        return requests.post(
            url,
            files=files,
            params=params,
            timeout=REQUEST_TIMEOUT,
        )
    finally:
        if opened_file:
            opened_file.close()


def seed_users() -> Dict[str, str]:
    log("Creating users...", Color.YELLOW)

    users = [
        ("admin_alice", "Admin1!", "Alice", "Admin", "Dallas"),
        ("super_sam", "Super1!", "Sam", "Supervisor", "Fort Worth"),
        ("rockPigeonLover41", "Bird1!", "Riley", "Pigeon", "Dallas"),
        ("taylor_b", "Bird1!", "Taylor", "Birds", "Irving"),
        ("jordanlee2", "Bird1!", "Jordan", "Lee", "Houston"),
        ("camila_ro", "Bird1!", "Camila", "Rojas", "Austin"),
        ("owl_at_dawn", "Bird1!", "Owen", "Dawn", "Sugar Land"),
    ]

    user_map: Dict[str, str] = {}
    profile_photo = first_profile_image()

    for username, password, first_name, last_name, location in users:
        user_id = ensure_user(username, password)
        if not user_id:
            continue

        user_map[username] = user_id

        session = login_session(username, password)
        if session:
            onboarding_ok = complete_onboarding(
                session,
                first_name,
                last_name,
                location,
                profile_photo,
            )
            if onboarding_ok:
                log(f"  ✓ Onboarding complete: {username}", Color.GREEN)
            else:
                log(f"  ⚠ Onboarding skipped/failed: {username}", Color.YELLOW)

    log(f"Created/resolved {len(user_map)} users", Color.GREEN)
    return user_map


def seed_birds() -> Dict[str, str]:
    log("Creating birds...", Color.YELLOW)

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

    bird_map: Dict[str, str] = {}

    for bird in birds:
        existing_bird_id = get_bird_id_by_common_name(bird["commonName"])
        if existing_bird_id:
            bird_map[bird["commonName"]] = existing_bird_id
            log(f"  ℹ Bird already exists: {bird['commonName']}", Color.YELLOW)
            continue

        image_path = find_bird_image(bird["commonName"])

        payload = dict(bird)
        if image_path is None:
            file_name = bird["commonName"].replace(" ", "_").replace("'", "") + ".jpg"
            payload["imageURL"] = (
                f"https://commons.wikimedia.org/wiki/Special:FilePath/{file_name}?width=600"
            )

        response = post_multipart_json(
            f"{API_GATEWAY}/birds",
            "bird",
            payload,
            image_path=image_path,
        )

        if response.status_code == 413 and image_path is not None:
            file_name = bird["commonName"].replace(" ", "_").replace("'", "") + ".jpg"
            fallback_payload = {
                "commonName": bird["commonName"],
                "scientificName": bird["scientificName"],
                "imageURL": f"https://commons.wikimedia.org/wiki/Special:FilePath/{file_name}?width=600",
            }
            log(
                f"  ⚠ Image too large for {bird['commonName']}, retrying without upload",
                Color.YELLOW,
            )
            response = post_multipart_json(
                f"{API_GATEWAY}/birds",
                "bird",
                fallback_payload,
                image_path=None,
            )

        if response.status_code in (200, 201):
            response_payload = safe_json(response)
            bird_id = extract_id(response_payload)
            if bird_id:
                bird_map[bird["commonName"]] = bird_id
                log(f"  ✓ Bird created: {bird['commonName']}", Color.GREEN)
            else:
                log(f"  ⚠ Bird created but id missing: {bird['commonName']}", Color.YELLOW)
        elif response.status_code in (409, 422):
            existing_bird_id = get_bird_id_by_common_name(bird["commonName"])
            if existing_bird_id:
                bird_map[bird["commonName"]] = existing_bird_id
                log(f"  ℹ Bird already exists: {bird['commonName']}", Color.YELLOW)
            else:
                log(f"  ⚠ Bird exists but id lookup failed: {bird['commonName']}", Color.YELLOW)
        else:
            log(
                f"  ✗ Failed bird {bird['commonName']}: "
                f"{response.status_code} {response.text[:120]}",
                Color.RED,
            )

    log(f"Created {len(bird_map)} birds", Color.GREEN)
    return bird_map


def seed_groups(user_map: Dict[str, str]) -> Dict[str, str]:
    log("Creating groups...", Color.YELLOW)

    groups = [
        {
            "name": "DFW Birders",
            "owner": "admin_alice",
            "description": "General DFW sightings",
        },
        {
            "name": "Coastal Bird Committee",
            "owner": "super_sam",
            "description": "Coastal migration and shorebirds",
        },
        {
            "name": "Hill Country Spotters",
            "owner": "admin_alice",
            "description": "Hill country watch list",
        },
        {
            "name": "Gulf Coast Birding",
            "owner": "super_sam",
            "description": "Gulf coast hotspot reports",
        },
        {
            "name": "Metro Birdwatchers",
            "owner": "admin_alice",
            "description": "City birding updates",
        },
    ]

    group_map: Dict[str, str] = {}

    for group in groups:
        existing_group_id = get_group_id_by_name(group["name"])
        if existing_group_id:
            group_map[group["name"]] = existing_group_id
            log(f"  ℹ Group already exists: {group['name']}", Color.YELLOW)
            continue

        owner_id = user_map.get(group["owner"])
        if not owner_id:
            log(f"  ✗ Missing owner id: {group['owner']}", Color.RED)
            continue

        payload = {
            "name": group["name"],
            "description": group["description"],
        }

        response = None
        for attempt in range(12):
            response = post_multipart_json(
                f"{API_GATEWAY}/groups",
                "group",
                payload,
                image_path=None,
                params={"userId": owner_id},
            )

            is_discovery_race = (
                response.status_code == 400
                and "Failed to fetch user data" in response.text
                and "503" in response.text
            )
            if not is_discovery_race:
                break

            if attempt < 11:
                time.sleep(2)

        if response is not None and response.status_code in (200, 201):
            response_payload = safe_json(response)
            group_id = extract_id(response_payload)
            if group_id:
                group_map[group["name"]] = group_id
                log(f"  ✓ Group created: {group['name']}", Color.GREEN)
            else:
                log(f"  ⚠ Group created but id missing: {group['name']}", Color.YELLOW)
        elif response is not None and response.status_code in (409, 422):
            existing_group_id = get_group_id_by_name(group["name"])
            if existing_group_id:
                group_map[group["name"]] = existing_group_id
                log(f"  ℹ Group already exists: {group['name']}", Color.YELLOW)
            else:
                log(f"  ⚠ Group exists but id lookup failed: {group['name']}", Color.YELLOW)
        else:
            log(
                f"  ✗ Failed group {group['name']}: "
                f"{response.status_code if response is not None else 'N/A'} "
                f"{response.text[:120] if response is not None else ''}",
                Color.RED,
            )

    log(f"Created {len(group_map)} groups", Color.GREEN)
    return group_map


def seed_sightings(
    user_map: Dict[str, str],
    bird_map: Dict[str, str],
    group_map: Dict[str, str],
) -> None:
    log("Creating sightings...", Color.YELLOW)

    sightings = [
        {
            "username": "rockPigeonLover41",
            "bird": "Rock Pigeon",
            "group": "DFW Birders",
            "header": "Morning sighting at White Rock Lake",
            "textBody": "Caught a small flock skimming the water just after sunrise. The light was perfect!",
            "latitude": 32.8256,
            "longitude": -96.7166,
            "imageHint": "Rock Pigeon",
        },
        {
            "username": "taylor_b",
            "bird": "Bald Eagle",
            "group": "Metro Birdwatchers",
            "header": "Soaring over Trinity River",
            "textBody": "Gliding circles above the treeline for several minutes.",
            "latitude": 32.8145,
            "longitude": -96.7459,
            "imageHint": "Bald Eagle",
        },
        {
            "username": "jordanlee2",
            "bird": "Great Blue Heron",
            "group": "Coastal Bird Committee",
            "header": "Calm morning at the Aransas boardwalk",
            "textBody": "Glassy water and great behavior notes while feeding.",
            "latitude": 28.0206,
            "longitude": -96.9903,
            "imageHint": "Great Blue Heron",
        },
        {
            "username": "camila_ro",
            "bird": "Northern Cardinal",
            "group": "Hill Country Spotters",
            "header": "Backyard visitor",
            "textBody": "Bright red male visited the feeder this morning.",
            "latitude": 30.2649,
            "longitude": -97.7733,
            "imageHint": "Northern Cardinal",
        },
        {
            "username": "owl_at_dawn",
            "bird": "Barred Owl",
            "group": "DFW Birders",
            "header": "Surprise at Brazos Bend",
            "textBody": "Unexpected appearance; it lingered for several minutes.",
            "latitude": 29.3928,
            "longitude": -95.6083,
            "imageHint": "Barred Owl",
        },
    ]

    existing_headers = existing_sighting_headers()

    created_count = 0
    for sighting in sightings:
        if sighting["header"] in existing_headers:
            log(f"  ℹ Sighting already exists: {sighting['header']}", Color.YELLOW)
            continue

        user_id = user_map.get(sighting["username"])
        bird_id = bird_map.get(sighting["bird"])
        group_id = group_map.get(sighting["group"])

        if not (user_id and bird_id and group_id):
            log(f"  ✗ Missing IDs for sighting by {sighting['username']}", Color.RED)
            continue

        payload = {
            "header": sighting["header"],
            "textBody": sighting["textBody"],
            "bird": bird_id,
            "group": group_id,
            "tags": {
                "latitude": str(sighting["latitude"]),
                "longitude": str(sighting["longitude"]),
            },
        }

        image_path = find_bird_image(sighting["imageHint"])
        response = post_multipart_json(
            f"{API_GATEWAY}/sightings",
            "post",
            payload,
            image_path=image_path,
            params={"userId": user_id},
        )

        if response.status_code in (200, 201):
            created_count += 1
            log(f"  ✓ Sighting created: {sighting['header']}", Color.GREEN)
        else:
            log(
                f"  ✗ Failed sighting {sighting['header']}: "
                f"{response.status_code} {response.text[:120]}",
                Color.RED,
            )

    log(f"Created {created_count} sightings", Color.GREEN)


def main() -> None:
    log("Starting BirdBook microservices seeding...", Color.YELLOW)
    wait_for_services()

    user_map = seed_users()
    if not user_map:
        log("No users created; stopping.", Color.RED)
        sys.exit(1)

    apply_seed_roles(user_map)

    bird_map = seed_birds()
    group_map = seed_groups(user_map)
    seed_sightings(user_map, bird_map, group_map)

    log("✓ Data seeding completed", Color.GREEN)
    log(
        f"Summary: users={len(user_map)}, birds={len(bird_map)}, groups={len(group_map)}",
        Color.GREEN,
    )


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        log("Interrupted by user", Color.RED)
        sys.exit(1)
    except Exception as exc:
        log(f"Seeding failed: {exc}", Color.RED)
        sys.exit(1)
