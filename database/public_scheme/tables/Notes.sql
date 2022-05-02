CREATE TABLE "Notes" (
  "id" SERIAL8 PRIMARY KEY,
  "sum" float4 NOT NULL,
  "currency_code" varchar NOT NULL DEFAULT 'UZS',
  "category" varchar NOT NULL,
  "user_id" int NOT NULL,
  "note_date" date NOT NULL,
  "comment" varchar,
  "created_on" timestamp DEFAULT (now()),
  "external_id" int
);

ALTER TABLE "Notes" ADD FOREIGN KEY ("user_id") REFERENCES "Users" ("id");
ALTER TABLE "Notes" ADD FOREIGN KEY ("category") REFERENCES "Categories" ("code");
ALTER TABLE "Notes" ADD FOREIGN KEY ("external_id") REFERENCES "Salary_income" ("id");

CREATE INDEX "Notes by user" ON "Notes" ("user_id");


COMMENT ON COLUMN "Notes"."type" IS '(I)ncome/(O)utcome';
COMMENT ON COLUMN "Notes"."note_date" IS 'Date of income/outcome';
COMMENT ON COLUMN "Notes"."created_on" IS 'Time of recording';
COMMENT ON COLUMN "Notes"."external_id" IS 'ID for relating multiple notes to one record in external table';
COMMENT ON COLUMN "Notes"."user_id" IS 'User ID of whom this record belongs';
