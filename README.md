# Situated Problem for [Phrag](https://github.com/ykskb/phrag) : SNS Project (Backend)

This project is backend of a situated problem for project: [Phrag](https://github.com/ykskb/phrag) to inspect its concept & validate the practicality.

Frontend codes (Next JS) sit in the repository [here](https://github.com/ykskb/situated-sns-frontend).

### Features

- **Phrag's GraphQL:** flexible query features to make one request per page easy.
  - Support for queries with extensive nests.
  - Support for pagination & filter arguments even on nested objects.
- **Phrag's Interceptors:** data should be created & accessed with authentication.
  - Access controls to data per an authenticated user.
  - Owner update for mutations from an authenticated user's ID.
  - Count updates on other tables per data creation.
  - Validation of conditions for user chat record creation.
- **Custom middleware:** auth contexts.
  - Context update with user info after validating Firebase auth token.
  - GraphQL arg mapping from `slug` to `user_id`. 
- **Custom routes:** endpoints for authentication and file handling.
  - Authed user retrieval
  - Profile image upload
  - Post image upload
  - Image file serving

### Run

Run `resources/migration/sns.sql` in a database of your choice.

Set env vars according to `project.clj` and run compiled jar.

Tests are not written for this project at the moment as it is merely for concept validation. Any help is welcomed though.
