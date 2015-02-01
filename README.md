#Overview
This is a simple application to retrieve a product information from the store using a product ID.

##Setup
1. Install [MongoDB](http://www.mongodb.org/downloads). The recommended version to use for this application is 2.4.9.
2. Download and install [Scala](http://www.scala-lang.org/download/) and [SBT](http://www.scala-sbt.org/download.html).
3. Dump `samples/catalog.json` into MongoDB.
4. Change `id` field type from number to string otherwise autocomplete in the product search field would not function. Use the Mongo script in the appendix to change the field type.
5. Execute `sbt run` in project root directory.

## Usage
1. Logged on to [http://localhost:9000](http://localhost:9000)

## Appendix
### Mongo script to change product ID type to string.
    db.catalog.find({"id":{$type:16}}).forEach(function (x) {
      x.id = x.id.toString();
      db.catalog.save(x);
    });

