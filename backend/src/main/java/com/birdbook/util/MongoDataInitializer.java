package com.birdbook.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.birdbook.models.Role;
import com.mongodb.client.MongoCollection;

@Component
public class MongoDataInitializer implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    public MongoDataInitializer(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // ===== USERS =====
    private final ObjectId adminUser = new ObjectId();
    private final ObjectId superUser = new ObjectId();
    private final List<ObjectId> basicUsers = new ArrayList<>();
    private final Map<ObjectId, String> userNames = new HashMap<>();
    private final Map<ObjectId, String> userProfilePics = new HashMap<>();

    // ===== GROUPS =====
    private final ObjectId groupDFW = new ObjectId();
    private final ObjectId groupCoastal = new ObjectId();
    private final ObjectId groupHillCountry = new ObjectId();
    private final ObjectId groupGulfCoast = new ObjectId();
    private final ObjectId groupMetroBirders = new ObjectId();
    private final Map<String, ObjectId> groupIdsByName = new HashMap<>();

    // ===== BIRDS =====
    private final List<ObjectId> birds = new ArrayList<>();
    private final List<String> birdCommonNames = new ArrayList<>();

    // ===== POSTS =====
    private final List<ObjectId> posts = new ArrayList<>();
    Map<ObjectId, List<ObjectId>> postsByUser = new HashMap<>();

    @Override
    public void run(String... args) {
        MongoCollection<Document> birdsCollection =
                ConnectionHandler.getDatabase().getCollection("birds");
        if (birdsCollection.countDocuments() == 0) {
            populateBirds(birdsCollection);
            System.out.println("Bird data initialized successfully!");
        }

        MongoCollection<Document> usersCollection =
                ConnectionHandler.getDatabase().getCollection("users");
        if (usersCollection.countDocuments() == 0) {
            populateUsers(usersCollection);
            System.out.println("User data initialized successfully!");
        }

        MongoCollection<Document> groupsCollection =
                ConnectionHandler.getDatabase().getCollection("groups");
        if (groupsCollection.countDocuments() == 0) {
            populateGroups(groupsCollection);
            System.out.println("Group data initialized successfully!");
        }

        MongoCollection<Document> postsCollection =
                ConnectionHandler.getDatabase().getCollection("posts");
        if (postsCollection.countDocuments() == 0) {
            populatePosts(postsCollection);
            System.out.println("Post data initialized successfully!");
        }
    }

    // =====================================================
    // USERS
    // =====================================================
    private Document userDoc(ObjectId id, String username, String rawPassword, Role role, String profilePic, String firstName, String lastName, String location) {
        return new Document("_id", id)
                .append("username", username)
                .append("password", passwordEncoder.encode(rawPassword))
                .append("role", role.name())
                .append("profilePic", profilePic)
                .append("firstName", firstName)
                .append("lastName", lastName)
                .append("location", location)
                .append("onboardingComplete", true)
                .append("friends", List.of())
                .append("posts", List.of())
                .append("groups", List.of());
    }

    private void populateUsers(MongoCollection<Document> collection) {
        List<Document> docs = new ArrayList<>();

        userNames.put(adminUser, "admin_alice");
        userProfilePics.put(adminUser, "/profile_pictures/adminAlice.jpg");
        docs.add(userDoc(adminUser, "admin_alice", "Admin1!", Role.ADMIN_USER,
            "/profile_pictures/adminAlice.jpg", "Alice", "Anderson", "Austin, Travis County, Texas, United States"));

        userNames.put(superUser, "super_sam");
        userProfilePics.put(superUser, "/profile_pictures/superSam.jpg");
        docs.add(userDoc(superUser, "super_sam", "Super1!", Role.SUPER_USER,
            "/profile_pictures/superSam.jpg", "Sam", "Smith", "Dallas, Dallas County, Texas, United States"));

        ObjectId birdUser = new ObjectId();
        basicUsers.add(birdUser);
        userNames.put(birdUser, "rockPigeonLover41");
        userProfilePics.put(birdUser, "/profile_pictures/rockPigeon.jpg");

        docs.add(userDoc(
                birdUser,
                "rockPigeonLover41",
                "Bird1!",
                Role.BASIC_USER,
                "/profile_pictures/rockPigeon.jpg",
                "The",
                "Rock",
                "Dallas, Dallas County, Texas, United States"
        ));


        List<String> sampleUsernames = List.of(
            "taylor_b", "jordanlee2", "camila_ro", "noahh", "ava_r5",
            "mariag", "liam_k9", "emma_j", "lucasm", "sofia_p6",
            "ethan_w2", "olivia_c", "mason_t1", "mia_v", "isabellaq",
            "owl_at_dawn", "kestrel_kite7", "warblerwatch", "heron_haven5", "robinridge",
            "finch_finder9", "tern_trail", "egret_eye1", "sparrowspot", "cardinalcall2"
        );

        List<String[]> userDetails = List.of(
            new String[]{"Taylor", "Brown", "Houston, Harris County, Texas, United States"},
            new String[]{"Jordan", "Lee", "San Antonio, Bexar County, Texas, United States"},
            new String[]{"Camila", "Rodriguez", "Fort Worth, Tarrant County, Texas, United States"},
            new String[]{"Noah", "Harris", "El Paso, El Paso County, Texas, United States"},
            new String[]{"Ava", "Roberts", "Arlington, Tarlington County, Texas, United States"},
            new String[]{"Maria", "Garcia", "Corpus Christi, Nueces County, Texas, United States"},
            new String[]{"Liam", "Kennedy", "Plano, Collin County, Texas, United States"},
            new String[]{"Emma", "Johnson", "Garland, Dallas County, Texas, United States"},
            new String[]{"Lucas", "Martinez", "Laredo, Webb County, Texas, United States"},
            new String[]{"Sofia", "Perez", "Chandler, Maricopa County, Arizona, United States"},
            new String[]{"Ethan", "Wilson", "Lubbock, Lubbock County, Texas, United States"},
            new String[]{"Olivia", "Clark", "Irving, Dallas County, Texas, United States"},
            new String[]{"Mason", "Taylor", "Grapevine, Tarrant County, Texas, United States"},
            new String[]{"Mia", "Valdez", "McKinney, Collin County, Texas, United States"},
            new String[]{"Isabella", "Quinn", "Frisco, Collin County, Texas, United States"},
            new String[]{"Owen", "Hayes", "Austin, Travis County, Texas, United States"},
            new String[]{"Sophia", "King", "San Marcos, Hays County, Texas, United States"},
            new String[]{"Aiden", "Webb", "Rockport, Aransas County, Texas, United States"},
            new String[]{"Charlotte", "Morris", "Galveston, Galveston County, Texas, United States"},
            new String[]{"Mason", "Ridge", "New Braunfels, Comal County, Texas, United States"},
            new String[]{"Amelia", "Finch", "Wimberley, Hays County, Texas, United States"},
            new String[]{"Benjamin", "Trail", "Gruene, Comal County, Texas, United States"},
            new String[]{"Harper", "Egret", "Port Aransas, Nueces County, Texas, United States"},
            new String[]{"Lucas", "Sparrow", "Kerrville, Kerr County, Texas, United States"},
            new String[]{"Evelyn", "Cardinal", "Boerne, Kendall County, Texas, United States"}
        );

        List<String> defaultPics = List.of(
            "/profile_pictures/default1.jpg",
            "/profile_pictures/default2.jpg",
            "/profile_pictures/default3.png",
            "/profile_pictures/default4.png"
        );

        for (int i = 0; i < sampleUsernames.size(); i++) {
            String username = sampleUsernames.get(i);
            ObjectId id = new ObjectId();

            basicUsers.add(id);
            userNames.put(id, username);

            String profilePic = defaultPics.get(i % defaultPics.size());
            userProfilePics.put(id, profilePic);
            
            String[] details = userDetails.get(i);
            docs.add(userDoc(id, username, "Bird1!", Role.BASIC_USER, profilePic, details[0], details[1], details[2]));
        }

        collection.insertMany(docs);
    }

    // =====================================================
    // GROUPS
    // =====================================================

    private void populateGroups(MongoCollection<Document> collection) {
        groupIdsByName.put("DFW Birders", groupDFW);
        groupIdsByName.put("Coastal Bird Committee", groupCoastal);
        groupIdsByName.put("Hill Country Spotters", groupHillCountry);
        groupIdsByName.put("Gulf Coast Birding", groupGulfCoast);
        groupIdsByName.put("Metro Birdwatchers", groupMetroBirders);

        collection.insertMany(List.of(
                new Document("_id", groupDFW)
                        .append("name", "DFW Birders")
                        .append("owner", postUser(adminUser))
                .append("members", basicUsers.subList(0, 20).stream().map(this::postUser).toList())
                        .append("requests", List.of()),

                new Document("_id", groupCoastal)
                        .append("name", "Coastal Bird Committee")
                        .append("owner", postUser(superUser))
                .append("members", basicUsers.subList(5, 15).stream().map(this::postUser).toList())
                .append("requests", List.of()),

            new Document("_id", groupHillCountry)
                .append("name", "Hill Country Spotters")
                .append("owner", postUser(adminUser))
                .append("members", basicUsers.subList(10, 22).stream().map(this::postUser).toList())
                .append("requests", List.of()),

            new Document("_id", groupGulfCoast)
                .append("name", "Gulf Coast Birding")
                .append("owner", postUser(superUser))
                .append("members", basicUsers.subList(0, 8).stream().map(this::postUser).toList())
                .append("requests", List.of()),

            new Document("_id", groupMetroBirders)
                .append("name", "Metro Birdwatchers")
                .append("owner", postUser(adminUser))
                .append("members", basicUsers.subList(3, 19).stream().map(this::postUser).toList())
                .append("requests", List.of())
        ));
    }

    // =====================================================
    // BIRDS
    // =====================================================

    private void populateBirds(MongoCollection<Document> collection) {
        // Array of [commonName, scientificName]
        String[][] birdData = {
            // Waterfowl
            {"Mallard", "Anas platyrhynchos"},
            {"Canada Goose", "Branta canadensis"},
            {"Wood Duck", "Aix sponsa"},
            {"American Black Duck", "Anas rubripes"},
            {"Northern Pintail", "Anas acuta"},
            {"Green-winged Teal", "Anas crecca"},
            {"Blue-winged Teal", "Spatula discors"},
            {"Gadwall", "Mareca strepera"},
            {"American Wigeon", "Mareca americana"},
            {"Northern Shoveler", "Spatula clypeata"},
            {"Canvasback", "Aythya valisineria"},
            {"Redhead", "Aythya americana"},
            {"Ring-necked Duck", "Aythya collaris"},
            {"Greater Scaup", "Aythya marila"},
            {"Lesser Scaup", "Aythya affinis"},
            {"Bufflehead", "Bucephala albeola"},
            {"Common Goldeneye", "Bucephala clangula"},
            {"Hooded Merganser", "Lophodytes cucullatus"},
            {"Common Merganser", "Mergus merganser"},
            {"Red-breasted Merganser", "Mergus serrator"},
            
            // Upland Game Birds
            {"Wild Turkey", "Meleagris gallopavo"},
            {"Ring-necked Pheasant", "Phasianus colchicus"},
            {"Ruffed Grouse", "Bonasa umbellus"},
            {"Northern Bobwhite", "Colinus virginianus"},
            {"Scaled Quail", "Callipepla squamata"},
            {"California Quail", "Callipepla californica"},
            {"Gambel's Quail", "Callipepla gambelii"},
            {"Mourning Dove", "Zenaida macroura"},
            {"Rock Pigeon", "Columba livia"},
            {"Eurasian Collared-Dove", "Streptopelia decaocto"},
            
            // Herons & Egrets
            {"Great Blue Heron", "Ardea herodias"},
            {"Great Egret", "Ardea alba"},
            {"Snowy Egret", "Egretta thula"},
            {"Little Blue Heron", "Egretta caerulea"},
            {"Tricolored Heron", "Egretta tricolor"},
            {"Cattle Egret", "Bubulcus ibis"},
            {"Green Heron", "Butorides virescens"},
            {"Black-crowned Night-Heron", "Nycticorax nycticorax"},
            {"Yellow-crowned Night-Heron", "Nyctanassa violacea"},
            
            // Raptors
            {"Turkey Vulture", "Cathartes aura"},
            {"Black Vulture", "Coragyps atratus"},
            {"Osprey", "Pandion haliaetus"},
            {"Bald Eagle", "Haliaeetus leucocephalus"},
            {"Northern Harrier", "Circus hudsonius"},
            {"Sharp-shinned Hawk", "Accipiter striatus"},
            {"Cooper's Hawk", "Accipiter cooperii"},
            {"Red-shouldered Hawk", "Buteo lineatus"},
            {"Broad-winged Hawk", "Buteo platypterus"},
            {"Red-tailed Hawk", "Buteo jamaicensis"},
            {"Rough-legged Hawk", "Buteo lagopus"},
            {"Golden Eagle", "Aquila chrysaetos"},
            {"American Kestrel", "Falco sparverius"},
            {"Merlin", "Falco columbarius"},
            {"Peregrine Falcon", "Falco peregrinus"},
            
            // Shorebirds
            {"Killdeer", "Charadrius vociferus"},
            {"American Avocet", "Recurvirostra americana"},
            {"Black-necked Stilt", "Himantopus mexicanus"},
            {"Spotted Sandpiper", "Actitis macularius"},
            {"Greater Yellowlegs", "Tringa melanoleuca"},
            {"Lesser Yellowlegs", "Tringa flavipes"},
            {"Willet", "Tringa semipalmata"},
            {"Sanderling", "Calidris alba"},
            {"Dunlin", "Calidris alpina"},
            {"Least Sandpiper", "Calidris minutilla"},
            
            // Gulls & Terns
            {"Ring-billed Gull", "Larus delawarensis"},
            {"Herring Gull", "Larus argentatus"},
            {"Great Black-backed Gull", "Larus marinus"},
            {"Laughing Gull", "Leucophaeus atricilla"},
            {"Bonaparte's Gull", "Chroicocephalus philadelphia"},
            {"Caspian Tern", "Hydroprogne caspia"},
            {"Common Tern", "Sterna hirundo"},
            {"Forster's Tern", "Sterna forsteri"},
            {"Least Tern", "Sternula antillarum"},
            {"Black Tern", "Chlidonias niger"},
            
            // Owls
            {"Great Horned Owl", "Bubo virginianus"},
            {"Eastern Screech-Owl", "Megascops asio"},
            {"Western Screech-Owl", "Megascops kennicottii"},
            {"Barred Owl", "Strix varia"},
            {"Barn Owl", "Tyto alba"},
            {"Long-eared Owl", "Asio otus"},
            {"Short-eared Owl", "Asio flammeus"},
            {"Northern Saw-whet Owl", "Aegolius acadicus"},
            {"Burrowing Owl", "Athene cunicularia"},
            
            // Woodpeckers
            {"Red-headed Woodpecker", "Melanerpes erythrocephalus"},
            {"Red-bellied Woodpecker", "Melanerpes carolinus"},
            {"Downy Woodpecker", "Dryobates pubescens"},
            {"Hairy Woodpecker", "Dryobates villosus"},
            {"Pileated Woodpecker", "Dryocopus pileatus"},
            {"Northern Flicker", "Colaptes auratus"},
            {"Yellow-bellied Sapsucker", "Sphyrapicus varius"},
            {"Acorn Woodpecker", "Melanerpes formicivorus"},
            
            // Flycatchers
            {"Eastern Phoebe", "Sayornis phoebe"},
            {"Say's Phoebe", "Sayornis saya"},
            {"Great Crested Flycatcher", "Myiarchus crinitus"},
            {"Western Kingbird", "Tyrannus verticalis"},
            {"Eastern Kingbird", "Tyrannus tyrannus"},
            {"Scissor-tailed Flycatcher", "Tyrannus forficatus"},
            {"Willow Flycatcher", "Empidonax traillii"},
            {"Least Flycatcher", "Empidonax minimus"},
            
            // Vireos
            {"White-eyed Vireo", "Vireo griseus"},
            {"Blue-headed Vireo", "Vireo solitarius"},
            {"Red-eyed Vireo", "Vireo olivaceus"},
            {"Warbling Vireo", "Vireo gilvus"},
            
            // Jays & Crows
            {"Blue Jay", "Cyanocitta cristata"},
            {"Steller's Jay", "Cyanocitta stelleri"},
            {"Western Scrub-Jay", "Aphelocoma californica"},
            {"American Crow", "Corvus brachyrhynchos"},
            {"Common Raven", "Corvus corax"},
            {"Fish Crow", "Corvus ossifragus"},
            {"Black-billed Magpie", "Pica hudsonia"},
            
            // Chickadees & Titmice
            {"Black-capped Chickadee", "Poecile atricapillus"},
            {"Carolina Chickadee", "Poecile carolinensis"},
            {"Mountain Chickadee", "Poecile gambeli"},
            {"Chestnut-backed Chickadee", "Poecile rufescens"},
            {"Tufted Titmouse", "Baeolophus bicolor"},
            {"Oak Titmouse", "Baeolophus inornatus"},
            {"Juniper Titmouse", "Baeolophus ridgwayi"},
            
            // Nuthatches & Creepers
            {"White-breasted Nuthatch", "Sitta carolinensis"},
            {"Red-breasted Nuthatch", "Sitta canadensis"},
            {"Brown-headed Nuthatch", "Sitta pusilla"},
            {"Pygmy Nuthatch", "Sitta pygmaea"},
            {"Brown Creeper", "Certhia americana"},
            
            // Wrens
            {"Carolina Wren", "Thryothorus ludovicianus"},
            {"House Wren", "Troglodytes aedon"},
            {"Winter Wren", "Troglodytes hiemalis"},
            {"Marsh Wren", "Cistothorus palustris"},
            {"Bewick's Wren", "Thryomanes bewickii"},
            {"Cactus Wren", "Campylorhynchus brunneicapillus"},
            {"Rock Wren", "Salpinctes obsoletus"},
            {"Canyon Wren", "Catherpes mexicanus"},
            
            // Thrushes
            {"Eastern Bluebird", "Sialia sialis"},
            {"Western Bluebird", "Sialia mexicana"},
            {"Mountain Bluebird", "Sialia currucoides"},
            {"Townsend's Solitaire", "Myadestes townsendi"},
            {"Veery", "Catharus fuscescens"},
            {"Hermit Thrush", "Catharus guttatus"},
            {"Wood Thrush", "Hylocichla mustelina"},
            {"American Robin", "Turdus migratorius"},
            {"Varied Thrush", "Ixoreus naevius"},
            
            // Mockingbirds & Thrashers
            {"Gray Catbird", "Dumetella carolinensis"},
            {"Northern Mockingbird", "Mimus polyglottos"},
            {"Brown Thrasher", "Toxostoma rufum"},
            {"Curve-billed Thrasher", "Toxostoma curvirostre"},
            
            // Starlings & Waxwings
            {"European Starling", "Sturnus vulgaris"},
            {"Cedar Waxwing", "Bombycilla cedrorum"},
            {"Bohemian Waxwing", "Bombycilla garrulus"},
            
            // Warblers
            {"Yellow Warbler", "Setophaga petechia"},
            {"Yellow-rumped Warbler", "Setophaga coronata"},
            {"Black-and-white Warbler", "Mniotilta varia"},
            {"American Redstart", "Setophaga ruticilla"},
            {"Common Yellowthroat", "Geothlypis trichas"},
            {"Prothonotary Warbler", "Protonotaria citrea"},
            {"Pine Warbler", "Setophaga pinus"},
            {"Palm Warbler", "Setophaga palmarum"},
            {"Blackpoll Warbler", "Setophaga striata"},
            {"Black-throated Green Warbler", "Setophaga virens"},
            {"Orange-crowned Warbler", "Leiothlypis celata"},
            {"Nashville Warbler", "Leiothlypis ruficapilla"},
            {"Tennessee Warbler", "Leiothlypis peregrina"},
            {"Northern Parula", "Setophaga americana"},
            {"Magnolia Warbler", "Setophaga magnolia"},
            {"Chestnut-sided Warbler", "Setophaga pensylvanica"},
            
            // Sparrows
            {"Eastern Towhee", "Pipilo erythrophthalmus"},
            {"Spotted Towhee", "Pipilo maculatus"},
            {"Chipping Sparrow", "Spizella passerina"},
            {"Field Sparrow", "Spizella pusilla"},
            {"American Tree Sparrow", "Spizelloides arborea"},
            {"Fox Sparrow", "Passerella iliaca"},
            {"Song Sparrow", "Melospiza melodia"},
            {"Lincoln's Sparrow", "Melospiza lincolnii"},
            {"Swamp Sparrow", "Melospiza georgiana"},
            {"White-throated Sparrow", "Zonotrichia albicollis"},
            {"White-crowned Sparrow", "Zonotrichia leucophrys"},
            {"Dark-eyed Junco", "Junco hyemalis"},
            {"Savannah Sparrow", "Passerculus sandwichensis"},
            {"Grasshopper Sparrow", "Ammodramus savannarum"},
            {"Lark Sparrow", "Chondestes grammacus"},
            {"Vesper Sparrow", "Pooecetes gramineus"},
            
            // Cardinals & Allies
            {"Northern Cardinal", "Cardinalis cardinalis"},
            {"Pyrrhuloxia", "Cardinalis sinuatus"},
            {"Rose-breasted Grosbeak", "Pheucticus ludovicianus"},
            {"Black-headed Grosbeak", "Pheucticus melanocephalus"},
            {"Blue Grosbeak", "Passerina caerulea"},
            {"Indigo Bunting", "Passerina cyanea"},
            {"Painted Bunting", "Passerina ciris"},
            {"Dickcissel", "Spiza americana"},
            
            // Blackbirds & Orioles
            {"Red-winged Blackbird", "Agelaius phoeniceus"},
            {"Eastern Meadowlark", "Sturnella magna"},
            {"Western Meadowlark", "Sturnella neglecta"},
            {"Yellow-headed Blackbird", "Xanthocephalus xanthocephalus"},
            {"Brewer's Blackbird", "Euphagus cyanocephalus"},
            {"Common Grackle", "Quiscalus quiscula"},
            {"Great-tailed Grackle", "Quiscalus mexicanus"},
            {"Brown-headed Cowbird", "Molothrus ater"},
            {"Orchard Oriole", "Icterus spurius"},
            {"Baltimore Oriole", "Icterus galbula"},
            {"Bullock's Oriole", "Icterus bullockii"},
            
            // Finches
            {"House Finch", "Haemorhous mexicanus"},
            {"Purple Finch", "Haemorhous purpureus"},
            {"Cassin's Finch", "Haemorhous cassinii"},
            {"American Goldfinch", "Spinus tristis"},
            {"Lesser Goldfinch", "Spinus psaltria"},
            {"Pine Siskin", "Spinus pinus"},
            {"Evening Grosbeak", "Coccothraustes vespertinus"},
            {"Common Redpoll", "Acanthis flammea"},
            
            // Old World Sparrows
            {"House Sparrow", "Passer domesticus"}
        };

        List<Document> docs = new ArrayList<>();

        for (String[] bird : birdData) {
            ObjectId birdId = new ObjectId();
            this.birds.add(birdId);

            String commonName = bird[0];
            String scientificName = bird[1];
            this.birdCommonNames.add(commonName);

            // Convert to Wikimedia filename format
            String fileName = commonName
                    .replace("’", "")
                    .replace("'", "")
                    .replace("-", "_")
                    .replace(" ", "_")
                    + ".jpg";

            String imageUrl =
                    "https://commons.wikimedia.org/wiki/Special:FilePath/"
                    + fileName
                    + "?width=600";

            docs.add(new Document("_id", birdId)
                    .append("commonName", commonName)
                    .append("scientificName", scientificName)
                    .append("imageURL", imageUrl));
        }

        collection.insertMany(docs);
    }

    // =====================================================
    // POSTS + COMMENTS
    // =====================================================

    private void populatePosts(MongoCollection<Document> collection) {
        Random rand = new Random();
        List<Document> docs = new ArrayList<>();

        long baseTime = System.currentTimeMillis() - 3_000_000_000L;

        if (groupIdsByName.isEmpty()) {
            MongoCollection<Document> groupsCollection =
                    ConnectionHandler.getDatabase().getCollection("groups");
            for (Document groupDoc : groupsCollection.find()) {
                String name = groupDoc.getString("name");
                ObjectId id = groupDoc.getObjectId("_id");
                if (name != null && id != null) {
                    groupIdsByName.put(name, id);
                }
            }
        }

        if (basicUsers.isEmpty() || userNames.isEmpty()) {
            MongoCollection<Document> usersCollection =
                    ConnectionHandler.getDatabase().getCollection("users");
            List<ObjectId> loadedBasics = new ArrayList<>();
            for (Document userDoc : usersCollection.find()) {
                ObjectId userId = userDoc.getObjectId("_id");
                String username = userDoc.getString("username");
                String profilePic = userDoc.getString("profilePic");
                String role = userDoc.getString("role");
                if (userId != null && username != null) {
                    userNames.put(userId, username);
                    if (profilePic != null) {
                        userProfilePics.put(userId, profilePic);
                    }
                    if (role != null && role.equals(Role.BASIC_USER.name())) {
                        loadedBasics.add(userId);
                    }
                }
            }
            if (!loadedBasics.isEmpty()) {
                basicUsers.clear();
                basicUsers.addAll(loadedBasics);
            } else if (basicUsers.isEmpty()) {
                basicUsers.addAll(userNames.keySet());
            }
        }

        if (basicUsers.isEmpty()) {
            return;
        }

        if (birds.isEmpty()) {
            MongoCollection<Document> birdsCollection =
                    ConnectionHandler.getDatabase().getCollection("birds");
            for (Document birdDoc : birdsCollection.find()) {
                ObjectId birdId = birdDoc.getObjectId("_id");
                String commonName = birdDoc.getString("commonName");
                if (birdId != null && commonName != null) {
                    birds.add(birdId);
                    birdCommonNames.add(commonName);
                }
            }
        }

        Map<String, ObjectId> birdNameToId = new HashMap<>();
        for (int i = 0; i < birdCommonNames.size(); i++) {
            birdNameToId.put(birdCommonNames.get(i), birds.get(i));
        }

        Map<String, String> birdImagePaths = new HashMap<>();
        birdImagePaths.put("Bald Eagle", "/images/baldEagle.jpg");
        birdImagePaths.put("Barred Owl", "/images/barredOwl.jpg");
        birdImagePaths.put("Bewick's Wren", "/images/bewickWren.jpg");
        birdImagePaths.put("Black-necked Stilt", "/images/blackNeckedStilt.jpg");
        birdImagePaths.put("Brown-headed Nuthatch", "/images/brownHeadedNuthatch.jpg");
        birdImagePaths.put("Western Scrub-Jay", "/images/californiaScrubJay.jpg");
        birdImagePaths.put("Cattle Egret", "/images/cattleEgret.jpg");
        birdImagePaths.put("Common Tern", "/images/commonTern.jpg");
        birdImagePaths.put("Eastern Phoebe", "/images/easternPhoebe.jpg");
        birdImagePaths.put("Gadwall", "/images/gadwall.jpg");
        birdImagePaths.put("Least Flycatcher", "/images/leastFlycatcher.jpg");
        birdImagePaths.put("Least Sandpiper", "/images/leastSandpiper.jpg");
        birdImagePaths.put("Lesser Scaup", "/images/lesserScaup.jpg");
        birdImagePaths.put("Mallard", "/images/mallard.jpg");
        birdImagePaths.put("Mountain Chickadee", "/images/mountainChickadee.jpg");
        birdImagePaths.put("Red-bellied Woodpecker", "/images/redBelliedWoodpecker.jpg");
        birdImagePaths.put("Ring-necked Pheasant", "/images/ringNeckedPheasant.jpg");
        birdImagePaths.put("Rock Pigeon", "/images/rockPigeon.jpg");
        birdImagePaths.put("Rough-legged Hawk", "/images/roughLeggedHawk.jpg");
        birdImagePaths.put("Townsend's Solitaire", "/images/townsendsSolitaire.jpg");

        List<Integer> birdsWithImages = new ArrayList<>();
        for (int i = 0; i < birdCommonNames.size(); i++) {
            String name = birdCommonNames.get(i);
            if (birdImagePaths.containsKey(name)) {
                birdsWithImages.add(i);
            }
        }

        class PostSeed {
            final String header;
            final String textBody;
            final double latitude;
            final double longitude;
            final String groupName;
            final boolean help;
            final boolean includeBirdName;
            final String birdName;
            final String imagePath;

            PostSeed(String header, String textBody, double latitude, double longitude, String groupName, boolean help, boolean includeBirdName, String birdName, String imagePath) {
            this.header = header;
            this.textBody = textBody;
            this.latitude = latitude;
            this.longitude = longitude;
            this.groupName = groupName;
            this.help = help;
            this.includeBirdName = includeBirdName;
            this.birdName = birdName;
            this.imagePath = imagePath;
            }
        }

        List<PostSeed> seeds = List.of(
            new PostSeed("Early morning at White Rock Lake",
                "Caught a small flock skimming the water just after sunrise. The light was perfect and they were actively feeding along the shoreline. I lingered for about twenty minutes and watched them shift between the reeds and open water as the breeze picked up.",
                32.8256, -96.7166, "DFW Birders", false, true, "Mallard", "/images/mallard.jpg"),
            new PostSeed("Quick flyby at the Galveston pier",
                "Quick flyby over the pier—heard them before I saw them. Great reminder to listen for calls when scanning the surf.",
                29.3107, -94.7905, "Coastal Bird Committee", false, false, "Common Tern", "/images/commonTern.jpg"),
            new PostSeed("Pair working the Padre Island dunes",
                "Spotted a pair working the dunes and picking insects from the sparse grass. Stayed at a distance to avoid flushing.",
                26.1595, -97.1680, "Gulf Coast Birding", false, true, "Black-necked Stilt", "/images/blackNeckedStilt.jpg"),
            new PostSeed("Songbird in the Austin greenbelt",
                "Heard a clear, ringing song from the canopy. Took a bit of patience, but finally got a clean look through the leaves. It moved in short hops between branches and paused long enough for a quick sketch and note on the call pattern.",
                30.2649, -97.7733, "Hill Country Spotters", false, false, "Eastern Phoebe", "/images/easternPhoebe.jpg"),
            new PostSeed("Marsh activity in Corpus Christi",
                "Low tide revealed a lot of movement in the shallows. Plenty of feeding activity and a few great photo moments.",
                27.8006, -97.3964, "Gulf Coast Birding", false, true, "Cattle Egret", "/images/cattleEgret.jpg"),
            new PostSeed("Soaring over the Trinity River",
                "Gliding circles above the treeline for several minutes. Surprised to see it so close to the city.",
                32.8145, -96.7459, "Metro Birdwatchers", false, false, "Bald Eagle", "/images/baldEagle.jpg"),
            new PostSeed("Mixed flock on the Cedar Hill loop",
                "Found a mixed flock along the trail. Noted a clear field mark on the wing bars when it perched. The flock kept rotating through the understory, so I stayed still and let them come to me instead of chasing.",
                32.5885, -96.9561, "DFW Birders", false, true, "Red-bellied Woodpecker", "/images/redBelliedWoodpecker.jpg"),
            new PostSeed("Calm morning at the Aransas boardwalk",
                "Calm morning, glassy water. Great views and behavior notes—feeding method was easy to observe.",
                28.0206, -96.9903, "Coastal Bird Committee", false, false, "Gadwall", "/images/gadwall.jpg"),
            new PostSeed("Surprise at Brazos Bend",
                "Wasn’t expecting to see this species here today. Stayed still and it lingered for a good five minutes.",
                29.3928, -95.6083, "Coastal Bird Committee", false, true, "Barred Owl", "/images/barredOwl.jpg"),
            new PostSeed("Fence line views at Katy Prairie",
                "Pair calling from the fence line. Spotted a second individual in the brush after a few minutes.",
                29.7858, -95.8244, "Hill Country Spotters", false, false, "Ring-necked Pheasant", "/images/ringNeckedPheasant.jpg"),
            new PostSeed("Sunset silhouette on the Austin shoreline",
                "A clean silhouette against the sunset. The flight pattern was distinctive and easy to confirm. It made a wide loop over the water twice before settling on a snag, which helped confirm the shape and tail pattern.",
                30.2500, -97.7500, "Hill Country Spotters", false, true, "Rough-legged Hawk", "/images/roughLeggedHawk.jpg"),
            new PostSeed("Short stop at the Frisco ponds",
                "Short visit but rewarding—steady activity around the reed edges. Submitted a checklist afterward.",
                33.1507, -96.8236, "Metro Birdwatchers", false, false, "Lesser Scaup", "/images/lesserScaup.jpg"),
            new PostSeed("Diving along the Grapevine Lake shoreline",
                "Foraging along the shoreline with quick dives. Nice contrast between plumage and the water. It surfaced with small prey a couple of times and briefly preened before moving on to the next cove.",
                32.9342, -97.0780, "DFW Birders", false, true, "Least Sandpiper", "/images/leastSandpiper.jpg"),
            new PostSeed("Quick look at McKinney Preserve",
                "Heard rustling in the shrubs and caught a quick look. Confirmed with a short burst of song.",
                33.1972, -96.6398, "Metro Birdwatchers", false, false, "Least Flycatcher", "/images/leastFlycatcher.jpg"),
            new PostSeed("Sandbar action at Matagorda Bay",
                "Wind picked up mid‑morning but the birds kept moving along the sandbar. Great behavior notes.",
                28.7092, -95.9494, "Coastal Bird Committee", false, true, "Common Tern", "/images/commonTern.jpg"),
            new PostSeed("Thermals over Big Bend",
                "High‑soaring and riding thermals for quite a while. Noted wing shape and tail pattern clearly. It climbed steadily without flapping for several minutes, then drifted east along the ridge line before dropping back into view.",
                29.1275, -103.2425, "Hill Country Spotters", true, false, "Townsend's Solitaire", "/images/townsendsSolitaire.jpg"),
            new PostSeed("Lunch break find on the San Antonio River Walk",
                "Quick stop during lunch and caught a surprise in the trees. Busy area, but it kept to a quiet pocket.",
                29.4239, -98.4936, "Hill Country Spotters", false, true, "Rock Pigeon", "/images/rockPigeon.jpg"),
            new PostSeed("Trailside perch at Lake Ray Roberts",
                "Nice close perch near the trail. Gave plenty of time for a photo and a few notes.",
                33.3657, -97.0331, "DFW Birders", false, false, "Bewick's Wren", "/images/bewickWren.jpg"),
            new PostSeed("Shoreline flocking at Gulf State Park",
                "Flocking behavior along the shoreline was interesting—kept an eye on spacing and feeding turns.",
                30.2387, -87.7150, "Gulf Coast Birding", false, true, "Brown-headed Nuthatch", "/images/brownHeadedNuthatch.jpg"),
            new PostSeed("Long drive to Falcon State Park",
                "Long drive but worth it. The habitat fit perfectly and the sighting was brief but unmistakable.",
                26.5445, -99.1450, "Gulf Coast Birding", true, false, "Mountain Chickadee", "/images/mountainChickadee.jpg")
        );

        for (int i = 0; i < seeds.size(); i++) {
            PostSeed seed = seeds.get(i);
            ObjectId postId = new ObjectId();
            posts.add(postId);

            ObjectId authorId = basicUsers.get(i % basicUsers.size());

            postsByUser
                    .computeIfAbsent(authorId, k -> new ArrayList<>())
                    .add(postId);

            ObjectId bird = birdNameToId.get(seed.birdName);
            if (bird == null) {
                continue;
            }
            String birdName = seed.birdName;

            String imagePath = seed.imagePath;
                String textBody = seed.includeBirdName
                    ? seed.textBody + " Noted a " + birdName + " in the area."
                    : seed.textBody;

            ObjectId groupId = groupIdsByName.get(seed.groupName);
            docs.add(new Document("_id", postId)
                .append("user", postUser(authorId))
                .append("header", seed.header)
                .append("bird", bird)
                .append("group", groupId)
                .append("flagged", false)
                .append("help", seed.help)
                .append("likes", List.of())
                .append("image", imagePath)
                .append("textBody", textBody)
                .append("timestamp", new Date(baseTime + (i * 86_400_000L)))
                .append("tags", new Document()
                    .append("latitude", seed.latitude)
                    .append("longitude", seed.longitude)
                )
                .append("comments", generateComments(rand, birdName))
            );
        }

        long now = System.currentTimeMillis();

        addSuperUserPosts(docs, now);
        addTrollPost(docs, now);

        MongoCollection<Document> usersCollection =
                ConnectionHandler.getDatabase().getCollection("users");

        for (Map.Entry<ObjectId, List<ObjectId>> entry : postsByUser.entrySet()) {
            usersCollection.updateOne(
                    new Document("_id", entry.getKey()),
                    new Document("$push",
                            new Document("posts",
                                    new Document("$each", entry.getValue())
                            )
                    )
            );
        }



        collection.insertMany(docs);
    }

    private List<Document> generateComments(Random rand, String birdName) {
        int count = rand.nextInt(4) + 1;
        List<Document> comments = new ArrayList<>();

        long baseTime = System.currentTimeMillis() - 500_000;

        List<String> templates = List.of(
            "Love the details on the " + birdName + " — great find!",
            "Nice spot! The " + birdName + " can be tricky to pick out.",
            "Thanks for sharing. I’ve only seen a " + birdName + " there once.",
            "Beautiful sighting — that " + birdName + " must’ve been exciting.",
            "Great notes! I’ll keep an eye out for the " + birdName + ".",
            "Awesome! The " + birdName + " has been on my list this season.",
            "Nice photo opportunity. The " + birdName + " is a favorite of mine.",
            "Lucky catch — I usually hear the " + birdName + " before I see it.",
            "Great write‑up — thanks for the location details!",
            "I was there last week and missed it. Nice find!",
            "Love the field notes — super helpful.",
            "The lighting sounds perfect for photos.",
            "Thanks for sharing! Adding this spot to my weekend list."
        );

        for (int i = 0; i < count; i++) {
            ObjectId uid = basicUsers.get(rand.nextInt(basicUsers.size()));

            comments.add(new Document("user", postUser(uid))
                    .append("textBody", templates.get(rand.nextInt(templates.size())))
                    .append("timestamp", new Date(baseTime + (i * 5_000)))
            );
        }

        return comments;
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private Document postUser(ObjectId id) {
        return new Document("userId", id)
                .append("username", userNames.get(id))
                .append("profilePic", userProfilePics.get(id));
    }

    private void addSuperUserPosts(List<Document> docs, long now) {

        // Safety check
        if (birds.size() < 9) {
            throw new IllegalStateException("Not enough birds to assign to super user posts");
        }

        ObjectId groupOwnedBySuper = groupCoastal;
        long baseTime = System.currentTimeMillis();

        List<String[]> dfw_locations = List.of(
            new String[]{"White Rock Lake", "32.8256", "-96.7166"},
            new String[]{"Cedar Hill State Park", "32.5885", "-96.9561"},
            new String[]{"Lake Ray Roberts", "33.3657", "-97.0331"},
            new String[]{"Grapevine Lake", "32.9342", "-97.0780"},
            new String[]{"Trinity River Audubon Center", "32.8145", "-96.7459"},
            new String[]{"Hagerman Wildlife Refuge", "33.7500", "-97.3167"},
            new String[]{"Lake Lavon", "33.2267", "-96.5000"},
            new String[]{"Benbrook Lake", "32.6545", "-97.4500"},
            new String[]{"Caddo National Grassland", "33.4500", "-97.6833"}
        );

        List<String[]> bird_images = List.of(
            new String[]{"Mallard", "/images/mallard.jpg"},
            new String[]{"Bald Eagle", "/images/baldEagle.jpg"},
            new String[]{"Great Blue Heron", "/images/californiaScrubJay.jpg"},
            new String[]{"Wood Duck", "/images/cattleEgret.jpg"},
            new String[]{"Red-tailed Hawk", "/images/gadwall.jpg"},
            new String[]{"Barred Owl", "/images/barredOwl.jpg"},
            new String[]{"Black-chinned Hummingbird", "/images/commonTern.jpg"},
            new String[]{"Carolina Wren", "/images/rockPigeon.jpg"},
            new String[]{"Northern Cardinal", "/images/mountainChickadee.jpg"}
        );

        for (int i = 0; i < 9; i++) {
            ObjectId postId = new ObjectId();
            ObjectId birdId = birds.get(i);

            postsByUser
                    .computeIfAbsent(superUser, k -> new ArrayList<>())
                    .add(postId);

            String[] location = dfw_locations.get(i);
            String[] bird_image = bird_images.get(i);

            docs.add(new Document("_id", postId)
                    .append("user", postUser(superUser))
                    .append("header", "Sighting at " + location[0])
                    .append("bird", birdId)
                    .append("group", groupOwnedBySuper)
                    .append("flagged", false)
                    .append("help", false)
                    .append("likes", List.of())
                    .append("image", bird_image[1])
                    .append("textBody", "Observed a notable " + bird_image[0] + " during a routine outing.")
                    .append("timestamp", new Date(baseTime - (365L * 86_400_000L)))
                    .append("tags", new Document()
                            .append("latitude", location[1])
                            .append("longitude", location[2]))
                    .append("comments", List.of())
            );
        }
    }


    private void addTrollPost(List<Document> docs, long now) {
        ObjectId postId = new ObjectId();
        ObjectId commenter = basicUsers.get(0);

        postsByUser
                .computeIfAbsent(basicUsers.get(1), k -> new ArrayList<>())
                .add(postId);

        docs.add(new Document("_id", postId)
                .append("user", postUser(basicUsers.get(1)))
                .append("header", "Rare sighting???")
                .append("bird", null)
                .append("group", groupDFW)
                .append("flagged", false)
                .append("help", false)
                .append("likes", List.of())
                .append("image", "/images/megastarrapter.jpg")
                .append("textBody", "Guys I found this awesome bird and it... MEGA EVOLVED")
                .append("timestamp", new Date(now - (20L * 86_400_000L))) // 3 weeks ago
                .append("tags", new Document("latitude", 32.5007).append("longitude", 94.7405))
                .append("comments", List.of(
                        new Document("user", postUser(commenter))
                                .append("textBody", "Erm... That's not a real bird... Awkward")
                                .append("timestamp", new Date(now - 86_000_000L))
                ))
        );
    }


}
