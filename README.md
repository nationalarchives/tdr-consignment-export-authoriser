## Consignment Export Authoriser
This is the project which contains the code for the consignment export authoriser 

The infrastructure for the export, is defined in the [terraform]("https://github.com/nationalarchives/tdr-terraform-environments") repository.
* The front end makes a request to the api gateway with the user's token
* A lambda authorises the request using the consignment api to check the token
* Api gateway triggers a step function
* The step function triggers an ECS task which runs the code in this repository.

This is the code for the second step. It does the following: 
* Gets the token from the request and the consignment ID from the path
* Calls the API with the token. If the consignment is returned then the user is authorised and Allow is returned, otherwise Deny is returned.

### Adding new environment variables to the tests
The environment variables in the deployed lambda are encrypted using KMS and then base64 encoded. These are then decoded in the lambda. Because of this, any variables in `src/test/resources/application.conf` which come from environment variables in `src/main/resources/application.conf` need to be stored base64 encoded. There are comments next to each variable to say what the base64 string decodes to. If you want to add a new variable you can run `echo -n "value of variable" | base64 -w 0` and paste the output into the test application.conf