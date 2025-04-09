package ru.netology.delivery.test;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import ru.netology.delivery.data.DataGenerator;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

class DeliveryTest {

    @BeforeAll
    static void setUpAll() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeListener("allure");
    }

    @BeforeEach
    void setup() {
        open("http://localhost:9999");
    }

    @Test
    @Story("Успешная отправка формы")
    @DisplayName("Should successful plan and replan meeting")
    void shouldSuccessfulPlanAndReplanMeeting() {
        var validUser = DataGenerator.Registration.generateUser("ru");
        var daysToAddForFirstMeeting = 4;
        var firstMeetingDate = DataGenerator.generateDate(daysToAddForFirstMeeting);
        var daysToAddForSecondMeeting = 7;
        var secondMeetingDate = DataGenerator.generateDate(daysToAddForSecondMeeting);

        fillForm(validUser, firstMeetingDate);
        submitForm();
        verifySuccessMessage(firstMeetingDate);
        newMeetingData(secondMeetingDate);
        submitForm();
        replanNotification();
        replan();
        verifySuccessMessage(secondMeetingDate);
    }

    @Step("Заполнить форму: {userInfo}, дата={date}")
    void fillForm(DataGenerator.UserInfo userInfo, String date) {

        // Заполнение поля "Город"
        $("[data-test-id='city'] input").setValue(userInfo.getCity());
        // Очистка и заполнение поля "Дата" первой встречи
        $("[data-test-id='date'] input").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input").setValue(date);
        // Заполнение поля "Имя"
        $("[data-test-id='name'] input").setValue(userInfo.getName());
        // Заполнение поля "Телефон"
        $("[data-test-id='phone'] input").setValue(userInfo.getPhone());
        // Отметка чекбокса согласия
        $("[data-test-id='agreement']").click();
    }

    @Step("Нажать кнопку 'Запланировать'")
    void submitForm() {
        // Нажатие кнопки "Запланировать"
        $("button.button").click();
    }

    @Step("Проверить успешное уведомление с запланированной датой {expectedDate}")
    void verifySuccessMessage(String expectedDate) {
        // Проверка, что появилось уведомление об успешном планировании встречи с датой firstMeetingDate
        $("[data-test-id='success-notification'] .notification__content")
                .shouldHave(Condition.text("Встреча успешно запланирована на " + expectedDate), Duration.ofSeconds(15))
                .shouldBe(Condition.visible);

        attachScreenshot("Успешное уведомление. Дата " + expectedDate);
    }

    @Step("Очистка и заполнение поля Дата новой датой {newMeetingDate}")
    void newMeetingData(String newMeetingDate) {
        // Очистка и заполнение поля "Дата" новой датой newMeetingDate
        $("[data-test-id='date'] input").doubleClick().sendKeys(Keys.BACK_SPACE);
        $("[data-test-id='date'] input").setValue(newMeetingDate);
    }

    @Step("Ожидание появления диалогового окна с предложением перепланировать встречу")
    void replanNotification() {
        $("[data-test-id='replan-notification'] .notification__content")
                .shouldHave(Condition.text("У вас уже запланирована встреча на другую дату. Перепланировать?"), Duration.ofSeconds(15))
                .shouldBe(Condition.visible);
    }

    @Step("Нажатие кнопки Перепланировать")
    void replan() {
        $("[data-test-id='replan-notification'] button").click();
    }

    @Attachment(value = "{attachName}", type = "image/png")
    byte[] attachScreenshot(String attachName) {
        return ((TakesScreenshot) Selenide.webdriver().object()).getScreenshotAs(OutputType.BYTES);
    }

}
