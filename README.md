# Custom Synapse Handler for WSO2 APIM

This is a simple custom handler for WSO2 API Manager/Micro Integrator.  
It processes incoming requests with `application/json` content type and performs the following:

- Converts into `text/plain`
- Reads the payload from the SOAP envelope's `<text>` element.
- Removes any keys prefixed with `@` in the JSON body.
- Replaces the payload with the cleaned JSON.
- Resets the `ContentType` and `messageType` appropriately.

## How to Build

1. Make sure you have Java and Maven installed.
2. Add required WSO2 and Synapse dependencies to your `pom.xml`.
3. Compile the project:

```bash
mvn clean install
```
4. Copy the built .jar file to the <APIM_HOME>/repository/components/dropins/
5. Create an API and add an additional property named customHandler with the value true. 
6. Modify the velocity_template.xml to ensure that the handler is applied only to APIs that have the customHandler property set to true.

```bash
<handlers xmlns="http://ws.apache.org/ns/synapse">
  #foreach($handler in $handlers)
    <handler xmlns="http://ws.apache.org/ns/synapse" class="$handler.className">
        #if($handler.hasProperties())
        #set ($map = $handler.getProperties() )
        #foreach($property in $map.entrySet())
        <property name="$!property.key" value="$!property.value"/>
        #end
        #end
    </handler>
    #if($apiObj.additionalProperties.get('customHandler') == "true" && $handler.className == "org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler")
    <handler class="org.wso2.carbon.test.CustomHandler"/>
    #end
  #end
</handlers>
```
