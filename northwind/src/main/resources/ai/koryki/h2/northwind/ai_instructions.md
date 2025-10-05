# Instructions for AI systems

## Userinteraction

Korykiai services should be executed after the user had committed 
the querystring in kql-format.

Always call validateQuery and show formatted querystring to the user.

Do not execute without prior showing the kql-query to let the user check.


Do not change formatting for querystrings returned by validateQuery-service.
Querystring can be displayed with syntax-highlighting like other programming languages, but
language identifier "kql" must be shown in code-boxes.

## Querygeneration

Do not try to evaluate Results of query-Service. Instead, try to include alle information
available inside the query.

Results should be displayed in table form. Do not explain or interpret results.

## Technical hints

All communication uses 'UTF-8' encoding.

## Special cases

### logical expressions instead of operation negation

For example kql doesn't support NOT IN operator directly.
Instead, logical expressions do support negation. Here is a running example:

    FIND products p
    WHERE NOT p.product_id IN (
     FIND order_details od, od-orders o
     WHERE o.order_date BETWEEN DATE '2023-01-01' AND DATE '2023-01-31'
     RETURN od.product_id
    )
    RETURN p.product_name



The northwind query service searches data in a backing schema.
It requires a custom query language called **kql**.

**kql** is a simplified query language using three major keywords, always in uppercase:

- FIND
  defines a directed graf of searched objects. FIND is always followed by an entity and an entityalias like: orders o.
  Optionally followed by a colon separated list of links like: orders o, o-customers c, o-products p

- WHERE
  defines a logical expression

- RETURN
  a colon separated list of output-expressions

Additionally, there are more keywords for special cases

- ORDER
  defines a colon separated o order-expressions

- AS
  defines one or more query-blocks

- INTERSECT, UNION, UNIONALL, MINUS
  defines set operations



**kql** is simplified and quite different from SQL, but its purpose is similar.


## How to use FIND keyword

Followed by FIND is a comma separated list of entity relation definitions.
Giving an example:

    FIND products p, p-orders o, o-orderdetails d     

The first entity is just the entitytype followed by an unique shortterm, often just the first character.
Entities and alias always use lowercase.

## How to use WHERE keyword

After keyword WHERE we define one logical expression. Again an example:

    WHERE p.product_name like 'A*' and o.order_date BETWEEN DATE '2025-01-01' AND DATE '2025-01-01' 

We reference the aliases defined in FIND-Clause followed by '.' and propertyname, also always lowercase.
We use SQL-like expression syntax.

## How to use RETURN keyword

After the third keyword RETURN we define a comma separated list for output. Given an example:

    RETURN p.product_name, d.unit_price * d.quantity

As shown here we can use expressions too.


## DATE literals

Use syntax:

    DATE '1970-01-01'

for Date-Values without TIME fragment.


Use syntax:

    TIMESTAMP '1970-01-01 00:00:00'
    TIMESTAMP '1970-01-01 00:00:00.000'
    TIMESTAMP '1970-01-01 00:00:00.000+02:00'

for Timestamp-Values TIME fragment.

Use syntax:

    TIME '00:00:00'
    TIME '00:00:00.000'
    TIME '00:00:00.000+02:00'

for Times-Values.


## Operators

### LIKE operator

Use '_' as wildcard for single letter.
Use '%' as wildcard for a sequence of letters.

### BETWEEN operator

Use this syntax for intervals.

    BETWEEN DATE '1970-01-01' AND DATE '1970-12-31'

### Negation

Do **not** try to negate operators directly, this will not work:

    o.order_date NOT BETWEEN DATE '1970-01-01' AND DATE '1970-12-31'

Use negation on level of logical expressions instead.

    NOT o.order_date BETWEEN DATE '1970-01-01' AND DATE '1970-12-31'

## Functions

**kql** supports a limited list of functions:

- via operator '+', '-', '*'. '/'
- sum
- count
- min
- max
- avg

do not use functions not mentioned in the list.


## Query Blocks

The main purpose of query blocks is better understanding,
but redundant query blocks confuse and must be avoided.
A good query block should either:

- Pre-calculate expensive operations for reuse
- Create intermediate results needed for complex joins
- Implement business logic that can't be done in a single query

## Joins

Joins are defined in WHERE Clause.

If there is only one link between two entities in 'descriptionOfLinks' then we can skip the join criteria.

The short form for joins is:

Use '-' for inner joins.
Use '+' for outer joins.

If there are more than one link between two entities defined, we have to name the link:

Use '-linkname-' for inner joins.
Use '-linkname+' for outer joins.

If possible prefer short form.
