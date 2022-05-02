CREATE TYPE "currency" AS ENUM (
  'USD',
  'RUB',
  'EUR',
  'TJS'
);

CREATE TABLE "Categories" (
  "id" SERIAL PRIMARY KEY,
  "code" varchar NOT NULL,
  "type" varchar NOT NULL,
  "label" varchar NOT NULL
);

CREATE TABLE "Users" (
  "id" SERIAL PRIMARY KEY,
  "login" varchar NOT NULL,
  "full_name" varchar NOT NULL,
  "password_hashed" varchar NOT NULL
);

CREATE TABLE "Notes" (
  "id" SERIAL PRIMARY KEY,
  "type" varchar NOT NULL,
  "sum" numeric NOT NULL,
  "category" varchar NOT NULL,
  "user_id" int NOT NULL,
  "note_date" date NOT NULL,
  "comment" varchar,
  "created_on" datetime DEFAULT (now()),
  "external_id" int
);

CREATE TABLE "Currency_deals" (
  "id" SERIAL PRIMARY KEY,
  "currency_type" currency NOT NULL DEFAULT 'USD',
  "currency_sum" numeric NOT NULL,
  "currency_rate" numeric NOT NULL
);

CREATE TABLE "Salary_income" (
  "id" SERIAL PRIMARY KEY,
  "sum" numeric
);

ALTER TABLE "Notes" ADD FOREIGN KEY ("user_id") REFERENCES "Users" ("id");

ALTER TABLE "Notes" ADD FOREIGN KEY ("category") REFERENCES "Categories" ("code");

ALTER TABLE "Notes" ADD FOREIGN KEY ("external_id") REFERENCES "Currency_deals" ("id");

ALTER TABLE "Notes" ADD FOREIGN KEY ("external_id") REFERENCES "Salary_income" ("id");

CREATE INDEX "Notes by user" ON "Notes" ("user_id");

COMMENT ON COLUMN "Categories"."type" IS '(I)ncome/(O)utcome';

COMMENT ON COLUMN "Notes"."type" IS '(I)ncome/(O)utcome';

COMMENT ON COLUMN "Notes"."note_date" IS 'Date of income/outcome';

COMMENT ON COLUMN "Notes"."created_on" IS 'Time of recording';

COMMENT ON COLUMN "Notes"."external_id" IS 'ID for relating multiple notes to one record in external table';

COMMENT ON COLUMN "Currency_deals"."currency_rate" IS 'rate of currency related to UZS at the moment of deal';
