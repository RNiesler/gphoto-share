[![CircleCI](https://circleci.com/gh/RNiesler/gphoto-share.svg?style=svg)](https://circleci.com/gh/RNiesler/gphoto-share)
[![codecov](https://codecov.io/gh/RNiesler/gphoto-share/branch/master/graph/badge.svg)](https://codecov.io/gh/RNiesler/gphoto-share)

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
* APP_ADMIN_EMAIL={email}

In CircleCI:
* Set the following environment variables
 * HEROKU_APP_NAME
 * HEROKU_API_KEY
 
For Web-Push:
* Run `./gradlew copy` to extract libs to build/lib
* Run `./generate-web-push-key.sh` to generate web-push key pair
* Set WEBPUSH_KEYS_PRIVATE and WEBPUSH_KEYS_PUBLIC accordingly.
* To test subscription:
`java -cp "build/lib/*" nl.martijndwars.webpush.cli.Cli send-notification --subscription="<result of subscribe on the client>" --publicKey="<generated public key>" --privateKey="<generated private key>"` 