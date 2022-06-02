package org.meta.dhu

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.{By, WebElement}

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDate}
import java.util.Properties
import scala.util.{Failure, Success, Try}

object CourtBooking {

  case class Element(className: String, textValue: String)

  final val driver = new ChromeDriver()
  final val props = new Properties()
  final val FacilityRentals = Element("rec1-catalog-tab-label", "Facility Rentals")
  final val RanchoPark = Element("rec1-catalog-group-name", "Rancho San Ramon Park")
  final val TennisCourt = Element("rec1-catalog-item-name", "Tennis Court 1 (Reservable)")
  final val AddToCartButton = Element("btn-success", "Add To Cart")
  final val CheckoutButton = Element("cart-checkout-button", "Checkout")
  final val SubmitButton = Element("checkout-continue-button", "Submit Responses")
  final val ReviewButton = Element("checkout-continue-button", "Review Transaction")
  final val CompleteButton = Element("checkout-continue-button", "Complete Transaction")
  final val TimeBlock = "Public Reservations 5pm-10pm ($0/Hour)"
  final val TimeFrom = "05:00 PM"
  final val TimeTo = "06:00 PM"

  def main(args: Array[String]): Unit = {
    // Load config file and clean up cookies
    init()
    // Log in
    val isLoginSuccess = login()
    if (!isLoginSuccess) {
      println("Log in failed.")
    }

    // Pick "Facility Rentals"
    seekAndClick(FacilityRentals, Some(RanchoPark))
    // Pick "Rancho San Ramon Park"
    seekAndClick(RanchoPark, Some(TennisCourt))
    // Pick "Tennis Court 1 (Reservable)"
    seekAndClick(TennisCourt, Some(AddToCartButton))
    // Pick date
    val t = LocalDate.now.plusDays(1)
    val d = t.format(DateTimeFormatter.ofPattern("d"))
    seekAndClick(Element("ui-state-default", d), Some(Element("ui-state-active", d)))

    Thread.sleep(1000)
    // Pick time block
    dropdownArrows("selectmenu-arrow", 0, TimeBlock)
    Thread.sleep(1000)

    // Pick time from
    dropdownArrows("selectmenu-arrow", 1, TimeFrom)
    Thread.sleep(1000)

    // Pick time to
    dropdownArrows("selectmenu-arrow", 1, TimeTo)
    Thread.sleep(1000)

    seekAndClick(AddToCartButton, Some(CheckoutButton))
    seekAndClick(CheckoutButton, Some(SubmitButton))
    driver.findElements(By.className("checkout-prompt-response")).forEach(_.click())
    seekAndClick(SubmitButton, Some(ReviewButton))
    seekAndClick(ReviewButton, Some(CompleteButton))
    seekAndClick(CompleteButton, None)
    driver.quit()

  }

  private def init(): Unit = {
    Try {
      val is = getClass.getClassLoader.getResourceAsStream("config.properties")
      props.load(is)
    } match {
      case Success(_) => println("Load config file successfully")
      case Failure(e) => println(e.getMessage)
    }
    driver.manage().deleteAllCookies()
  }

  private def login(): Boolean = {
    Try {
      driver.get(props.getProperty("tennis.court.url"))
      driver.findElement(By.className("dropdown-toggle")).click()
      driver.findElement(By.className("rec1-login-toggle-button")).click()
      val loginForm = driver.findElement(By.className("ui-inset-sm"))
      loginForm
        .findElement(By.id("login-username"))
        .sendKeys(props.getProperty("tennis.court.username"))
      loginForm
        .findElement(By.id("login-password"))
        .sendKeys(props.getProperty("tennis.court.password"))
      loginForm.findElement(By.className("btn-primary")).click()
      new WebDriverWait(driver, Duration.ofSeconds(3))
        .until(_ => isFound(FacilityRentals))
    }.toOption.getOrElse(false)
  }

  private def seek(element: Element): Array[WebElement] =
    driver
      .findElements(By.className(element.className))
      .toArray
      .filter(_.isInstanceOf[WebElement])
      .map(_.asInstanceOf[WebElement])
      .filter(_.getText == element.textValue)

  private def isFound(element: Element): Boolean =
    seek(element).nonEmpty

  private def seekAndClick(element: Element, expectElement: Option[Element]): Boolean = {
    val webElements = seek(element)
    if (webElements.isEmpty) {
      println(s"Element: ${element.textValue} is not found")
      false
    } else {
      if (webElements.length > 1) {
        println("More than 1 elements were selected")
      }
      webElements.head.click()
      expectElement match {
        case Some(e) =>
          Try {
            val res = new WebDriverWait(driver, Duration.ofSeconds(3)).until(_ => isFound(e))
            println(s"Clicked ${element.textValue} successfully")
            res
          }.toOption.getOrElse(false)
        case None => true
      }
    }
  }

  private def dropdownArrows(className: String, index: Int, textValue: String): Boolean = {
    val arrows = driver
      .findElements(By.className(className))
      .toArray
      .filter(_.isInstanceOf[WebElement])
      .map(_.asInstanceOf[WebElement])
    assert(arrows.length == 3, "The number of dropdown arrows should be 3")
    arrows(index).click()
    Try {
      val res = new WebDriverWait(driver, Duration.ofSeconds(3))
        .until { _ =>
          val s = driver.findElements(By.className("selectmenu-selectedItem")).toArray()
          s(index).asInstanceOf[WebElement].getText == textValue
        }
      println(s"Clicked dropdown arrow successfully")
      res
    }

    seekAndClick(
      Element("selectmenu-item", textValue),
      Some(Element("selectmenu-selectedItem", textValue))
    )
  }
}
