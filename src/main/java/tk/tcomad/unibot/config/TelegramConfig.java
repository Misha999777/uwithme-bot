package tk.tcomad.unibot.config;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@Configuration
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi() {
        ApiContextInitializer.init();
        return new TelegramBotsApi();
    }

    @Bean
    public DBContext telegramDbContext() {
        DB db = DBMaker.tempFileDB().closeOnJvmShutdown().transactionEnable().make();
        return new MapDBContext(db);
    }

}
