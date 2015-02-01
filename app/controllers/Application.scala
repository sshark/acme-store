package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONDocument

import scala.concurrent.Future

object Application extends Controller with MongoController {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def collection: JSONCollection = db.collection[JSONCollection]("catalog")

  def ids(itemId: Option[String]) = Action.async {
    val catalogList = itemId match {
      case Some(id) => matchIds(id)
      case None => matchAllIds
    }
    val futureJsonArray: Future[JsArray] = catalogList.map {id => Json.arr(id)}
    futureJsonArray.map (p => Ok(Json.arr(p(0).as[List[Map[String, String]]].map(_("id")))(0)))
  }

  def matchIds(id: String) = collection.find(Json.obj("id" -> Json.obj("$regex" -> id)), Json.obj("id" -> 1, "_id" -> 0)).cursor[JsObject].collect[List]()

  def matchAllIds = collection.find(Json.obj(), Json.obj("id" -> 1, "_id" -> 0)).cursor[JsObject].collect[List]()

  def items(id: String) = Action.async {
    val catalogItem = collection.find(Json.obj("id" -> id)).cursor[JsObject].collect[List]()
    val futureJsonArray: Future[JsArray] = catalogItem.map {id => Json.arr(id)}
    futureJsonArray.map { p => Ok(p(0))}
  }

  def updatePrice(id: String, newPrice: Double) =  Action.async {
    import play.modules.reactivemongo.json.BSONFormats._

    val selector = BSONDocument("id" -> id)

    val modifier = BSONDocument("$set" -> BSONDocument("pricing.price" -> newPrice))

    // get a future update
    val futureUpdate = collection.update(selector, modifier)
    futureUpdate.map(p =>
      Ok(s"{\42id\42:\42$id\42,\42action\42:\42update\42, \42item\42:\42price\42, \42value\42:$newPrice}")
        .as("application/json"))
  }

  def updateTitle(id: String, newTitle: String) = Action.async {
    import play.modules.reactivemongo.json.BSONFormats._

    val selector = BSONDocument("id" -> id)

    val modifier = BSONDocument("$set" -> BSONDocument("title" -> newTitle))

    // get a future update
    val futureUpdate = collection.update(selector, modifier)
    futureUpdate.map(p => Ok(s"{\42id\42:\42$id\42,\42action\42:\42update\42, \42item\42:\42title\42, \42value\42:\42$newTitle\42}")
      .as("application/json"))
  }
}

