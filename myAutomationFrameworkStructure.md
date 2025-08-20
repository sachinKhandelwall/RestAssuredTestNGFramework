# RestAssured API Automation Framework Structure

```plaintext

src
│
└── main
    │
    ├── java
    │   └── com.oneshop.bff
    │       ├── applicationApi  (contains methods to build payloads and module-specific API calls; these call generic 
    │       │   │                 RestUtils methods and return Response)
    │       │   ├── PersonalInfoApi.java
    │       │   │
    │       │   │   // Sample outline:
    │       │   │   public class PersonalInfoApi {
    │       │   │       private final ObjectMapper objectMapper = new ObjectMapper();
    │       │   │       private final Endpoints endpoint = new Endpoints(); // your endpoint resolver
    │       │   │
    │       │   │       public PersonalInfo personalInfoPayload() {
    │       │   │           // build and return the PersonalInfo request POJO
    │       │   │           return new PersonalInfo(/* set fields */);
    │       │   │       }
    │       │   │
    │       │   │       public Response postPersonalInfoForGamValidation(PersonalInfo payload) throws Exception {
    │       │   │           objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    │       │   │           String personalInfoPayload = objectMapper.writeValueAsString(payload);
    │       │   │           Response response = RestUtils.post(
    │       │   │                   endpoint.getEndpointPath("postPersonalDetails"),
    │       │   │                   personalInfoPayload
    │       │   │           );
    │       │   │           return response;
    │       │   │       }
    │       │   │
    │       │   │       public Response getContacts() {
    │       │   │           return RestUtils.get(endpoint.getEndpointPath("getContacts"));
    │       │   │       }
    │       │   │   }
    │       │   │
    │       │   ├── BasketApi.java
    │       │   └── CheckoutApi.java
    │       │
    │       │
    │       ├── pojo
    │       │   ├── request
    │       │   │   └── (POJO classes for request payloads)
    │       │   └── response
    │       │       └── (POJO classes for response payloads)
    │       │
    │       ├── apiClient
    │       │   ├── RestUtils.java  (generic POST/PUT/GET methods to perform CRUD operations; called from applicationApi classes)
    │       │   │
    │       │   │   // Signatures and sample implementation:
    │       │   │   public class RestUtils {
    │       │   │
    │       │   │       public static Response post(String path, String requestPayload) {}
    │       │   │
    │       │   │       public static Response post(String path, String token, String requestPayload) {
    │       │   │           return given(SpecBuilder.getRequestSpec())
    │       │   │                   .header("Authorization", "Bearer " + token)
    │       │   │                   // alternatively: .auth().oauth2(token)
    │       │   │                   .body(requestPayload)
    │       │   │                   .when().post(path)
    │       │   │                   .then().spec(SpecBuilder.getResponseSpec())
    │       │   │                   .extract().response();
    │       │   │       }
    │       │   │
    │       │   │       public static Response get(String path) {
    │       │   │           return given(SpecBuilder.getRequestSpec())
    │       │   │                   .when().get(path)
    │       │   │                   .then().spec(SpecBuilder.getResponseSpec())
    │       │   │                   .extract().response();
    │       │   │       }
    │       │   │
    │       │   ├── SpecBuilder.java  (methods for Request and Response specifications)
    │       │   │
    │       │   │   // Sample outline:
    │       │   │   public class SpecBuilder {
    │       │   │       public static RequestSpecification getRequestSpec() {
    │       │   │           return new RequestSpecBuilder()
    │       │   │                   // .setBaseUri("https://api.spotify.com")
    │       │   │                   .setBaseUri(System.getProperty("BASE_URI"))
    │       │   │                   .setBasePath(BASE_PATH)
    │       │   │                   .setContentType(ContentType.JSON)
    │       │   │                   .addFilter(new AllureRestAssured())
    │       │   │                   .log(LogDetail.ALL)
    │       │   │                   .build();
    │       │   │       }
    │       │   │
    │       │   │       public static ResponseSpecification getResponseSpec() {
    │       │   │           return new ResponseSpecBuilder()
    │       │   │                   .expectContentType(ContentType.JSON)
    │       │   │                   .log(LogDetail.ALL)
    │       │   │                   .build();
    │       │   │       }
    │       │   │   }
    │       │   │
    │       │   └── StatusCode.java  (Enum)
    │       │
    │       └── utils
    │           ├── ConfigLoader.java  (loads config like client_id, token from config.properties using Singleton pattern)
    │           │
    │           │   // Sample outline:
    │           │   public class ConfigLoader {
    │           │       private final Properties properties;
    │           │       private static ConfigLoader configLoader;
    │           │
    │           │       private ConfigLoader() {
    │           │           properties = PropertyUtils.propertyLoader("src/test/resources/config.properties");
    │           │       }
    │           │
    │           │       public static ConfigLoader getInstance() {
    │           │           if (configLoader == null) {
    │           │               configLoader = new ConfigLoader();
    │           │           }
    │           │           return configLoader;
    │           │       }
    │           │
    │           │       public String getUser() {
    │           │           String prop = properties.getProperty("user_id");
    │           │           if (prop != null) return prop;
    │           │           else throw new RuntimeException("property user_id not specified in the config.properties file");
    │           │       }
    │           │   }
    │           │
    │           ├── PropertyUtils.java   (read/load property files)
    │           ├── FakerUtils.java      (generate random name, address, etc.)
    │           ├── DateUtils.java
    │           ├── EncryptionUtil.java
    │           ├── YamlReader.java
│   │           ├── JsonReader.java
│   │           ├── ExcelUtil.java
src │           └── TokenManager.java
│
├── test
│   │
│   ├── java
│   │   └── com.oneshop.bff
│   │       ├── baseTest
│   │       │   └── BaseTest.java                # Base setup/teardown class for tests
│   │       │
│   │       └── testScripts
│   │           ├── tests                       # contains test scripts of a particular module say PI page
│   │           │   └── GamValidation_PersonalInfo.java    # Example test class (extends BaseTest)
│   │           │       # @Test public void verifyGamValidationWithValidAddress() {}
│   │           │
│   │           └── e2eTests
│   │               └── OrderPlacementTest.java   # Example E2E order placement test (tariff/device flow)
│   │
│   └── resources
│       ├── endpoints (or routes)                # contains all the endpoints of different modules
│       │   ├── TariffListingEndpoints.java      # Endpoints for tariff module 
│       │   ├── DeviceListingEndpoints.java      # Endpoints for device module
│       │   └── BasketEndpoints.java             # Endpoints for basket module
│       │
│       ├── constants                
│       │   ├── Constants.java                   # Constants like cms key name, expectedData name, etc.
│       │
│       └── testData                           # contains testData required for different envs
│           ├── stage.json                       # Test data for stage env
│           │
│           │                                    {
│           │                                          "basket": {
│           │                                           "preloadedCartKey": "69qmk98aKF",
│           │                                           "portationId": "3245"
│           │                                        },
│           │                                         "personalInfo": {
│           │                                          "phoneNumber": "2143525",
│           │                                          "cityForSuggestedAddress": "Gdcf3" 
│           │                                     }
│           │
│           ├── preprod.json                     # Test data for preprod env
│           └── prod.json                        # Test data for production env
│
└── pom.xml   
│
└── testng.xml    
│
└── reports    
