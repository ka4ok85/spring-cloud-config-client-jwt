# spring-cloud-config-client-jwt
## JWT minded Configuration Integration for Spring Cloud Config Client

### Features 
Spring Cloud Config Client JWT uses JWT authentication approach instead of standard Basic Authentication. Please keep in mind Spring Cloud Config Server needs some changes too. [Find sample code here](https://github.com/ka4ok85/seattle-open-data-spring-cloud-config-server)

Authentication flow has following steps:
  1. Client sends request with username/password to Server's Authentication REST Controller.
  2. Server returns back JWT.
  3. Client includes Token with *Bearer:* prefix into *Authorization* Header for querying configuration values from Config Server.

### Usage 
  1. Build with Maven using *clean package*
  2. Include spring-cloud-config-client-jwt and **spring-cloud-starter-config** into pom.xml for your project.
  3. Keep standard *spring.cloud.config.uri*, *spring.cloud.config.username*, *spring.cloud.config.password* and new **spring.cloud.config.enabled=false**, **spring.cloud.config.endpoint={YOUR_CONFIG_SERVER_URL}/auth** values into your *bootstrap.properties*

#### Sample bootstrap.properties:

   spring.application.name=my-service  
   spring.cloud.config.uri=http://localhost:8888  
   spring.cloud.config.username=user  
   spring.cloud.config.password=pwd  
   spring.cloud.config.enabled=false  
   spring.cloud.config.endpoint=http://localhost:8888/auth  
