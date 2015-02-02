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
      case Some(id) => matchingId(id)
      case None => matchAllIds
    }
    val futureJsonArray: Future[JsArray] = catalogList.map {id => Json.arr(id)}
    futureJsonArray.map (p => Ok(Json.arr(p(0).as[List[Map[String, String]]].map(_("id")))(0)))
  }

  def matchingId(id: String) = collection.find(Json.obj("id" -> Json.obj("$regex" -> id)), Json.obj("id" -> 1, "_id" -> 0)).cursor[JsObject].collect[List]()

  def matchAllIds = collection.find(Json.obj(), Json.obj("id" -> 1, "_id" -> 0)).cursor[JsObject].collect[List]()

  def items(id: String) = Action.async {
    val catalogItem = collection.find(Json.obj("id" -> id)).cursor[JsObject].collect[List]()
    val futureJsonArray: Future[JsArray] = catalogItem.map {id => Json.arr(id)}
    futureJsonArray.map { p => Ok(p(0))}
  }

  def updateTitle(id: String, newTitle: String) = Action.async {
    import play.modules.reactivemongo.json.BSONFormats._

    // TODO verify new title only contains alphanumeric characters, spaces and underscores before update

    val selector = BSONDocument("id" -> id)

    val modifier = BSONDocument("$set" -> BSONDocument("title" -> newTitle))

    // get a future update
    val futureUpdate = collection.update(selector, modifier)
    futureUpdate.map(p => Ok(buildResponse(id, "update", "title", newTitle)).as("application/json"))
  }

  def updatePrice(id: String, newPrice: Double) =  Action.async {
    import play.modules.reactivemongo.json.BSONFormats._

    // TODO verify new price against cost before update

    val selector = BSONDocument("id" -> id)

    val modifier = BSONDocument("$set" -> BSONDocument("pricing.price" -> newPrice))

    // get a future update
    val futureUpdate = collection.update(selector, modifier)

    futureUpdate.map(p => Ok(buildResponse(id, "update", "price", newPrice)).as("application/json"))
  }

  private def buildResponse(id: String, action: String, item: String, value: String) = {
    Json.obj("id" -> id, "action" -> action, "item" -> item, "value" -> value).toString()
  }

  private def buildResponse(id: String, action: String, item: String, value: Double) = {
    Json.obj("id" -> id, "action" -> action, "item" -> item, "value" -> value).toString()
  }
}
