CREATE TABLE "Users" (
  "id" SERIAL PRIMARY KEY,
  "login" varchar NOT NULL,
  "full_name" varchar NOT NULL,
  "password_hashed" varchar NOT NULL
);
