import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MessageTest {
    private static Bot bot;
    @Mock
    private static Update update;
    @Mock
    private static Message message;

    @org.junit.BeforeClass
    public static void setUp() {
        bot = new Bot("myToken");
        update = Mockito.mock(Update.class);
        message = Mockito.mock(Message.class);
    }

    @org.junit.Before
    public void before_incorrectMessageTest() {
        Mockito.when(update.getMessage()).thenReturn(message);
        Mockito.when(update.getMessage().hasText()).thenReturn(true);
        Mockito.when(update.getMessage().getText()).thenReturn("абырвалг");
    }

    @Test
    public void incorrectMessageTest() {
//        SendMessage res = (SendMessage) bot.readMessage(update);
//        assertEquals("Я не знаю такой команды((", res.getText());
    }

    @org.junit.After
    public void after_incorrectMessageTest() {
    }

    @org.junit.AfterClass
    public static void tearDown() {
    }
}
