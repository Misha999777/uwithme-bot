package tk.tcomad.unibot.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import tk.tcomad.unibot.entity.BotUser;

public interface BotUserRepository extends CrudRepository<BotUser, Long> {
    Optional<BotUser> findBotUserByUserId(String userId);
}
