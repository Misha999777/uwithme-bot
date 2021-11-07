package tk.tcomad.unibot.repository;

import org.springframework.data.repository.CrudRepository;
import tk.tcomad.unibot.entity.LoginSession;

public interface LoginSessionRepository extends CrudRepository<LoginSession, String> {
    LoginSession findLoginSessionBySub(String sub);
    void deleteAllByChatId(String chatId);
}
