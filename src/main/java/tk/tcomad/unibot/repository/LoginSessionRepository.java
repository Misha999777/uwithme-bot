package tk.tcomad.unibot.repository;

import org.springframework.data.repository.CrudRepository;
import tk.tcomad.unibot.entity.LoginSession;

public interface LoginSessionRepository extends CrudRepository<LoginSession, String> {
    LoginSession findLoginSessionByToken(String token);
    void deleteAllByChatId(String chatId);
}
