@echo off
set DATABASE_PASSWORD=test1234
set DATABASE_URL=jdbc:postgresql://localhost:5434/backend?schema=public
set DATABASE_USER=backend
set JWT_AUTH_SECRET=jwt_secret
set JWT_PASSWORD_RECOVERY_SECRET=jwt_password_recovery_secret
set JWT_VERIFICATION_SECRET=jwt_verification_secret
set SENDGRID_API_KEY=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
set SENDGRID_USERNAME=apikey
set SPRING_PROFILES_ACTIVE=prod

gradlew bootRun --args='--spring.profiles.active=prod'