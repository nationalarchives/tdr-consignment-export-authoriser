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