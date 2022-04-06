# Situated Problem for [Phrag](https://github.com/ykskb/phrag) : SNS Project (Backend)

This project is backend of a situated problem for project: [Phrag](https://github.com/ykskb/phrag) to inspect its concept & validate the practicality.

Frontend codes (Next JS) sit in the repository [here](https://github.com/ykskb/situated-sns-frontend).

### Features

- Phrag GraphQL Endpoint with interceptors
- Custom middleware:
  - Context update with user info after Firebase auth token validation
  - GraphQL arg mapping from `slug` to `user_id`
- Custom routes:
  - Authed user retrieval
  - Profile image upload
  - Post image upload
  - Image file serving

### Run

Run `resources/migration/sns.sql` in a database of your choice.

Set env vars according to `project.clj` and run compiled jar.

Tests are not written for this project at the moment as it is merely for concept validation. Any help is welcomed though.
