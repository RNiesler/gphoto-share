In Google API Console (https://console.developers.google.com/apis/dashboard):
* Create project
* Enable Photos Library API
* Generate OAuth2 credentials:
    * Add to "Authorized redirect URIs":
        * https://{app name}.herokuapp.com/login/oauth2/code/google
        * http://localhost:8080/login/oauth2/code/google


In Heroku:
Set the following config vars:
* SPRING_DATA_MONGODB_URI={proper mongo uri}
* SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT-ID={google api client id}
* SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT-SECRET={google api client secret}
* SPRING_REDIS_URL={proper redis urls}

In CircleCI:
* Set the following environment variables
 * HEROKU_APP_NAME
 * HEROKU_API_KEY