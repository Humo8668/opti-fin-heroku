CREATE TABLE "Categories" (
  "id" SERIAL PRIMARY KEY,
  "code" varchar NOT NULL,
  "type" varchar NOT NULL,
  "label" varchar NOT NULL
);

alter table "Categories" add unique ("code");

COMMENT ON COLUMN "Categories"."type" IS '(I)ncome/(O)utcome';