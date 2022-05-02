CREATE TABLE "Salary_income" (
  "id" SERIAL PRIMARY KEY,
  "company_name" varchar,
);

comment on column "Salary_income"."company_name" is 'Source of salary, name of company';