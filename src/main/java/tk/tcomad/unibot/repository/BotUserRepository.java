package tk.tcomad.unibot.repository;

import org.springframework.data.repository.CrudRepository;
import tk.tcomad.unibot.entity.BotUser;

public interface BotUserRepository extends CrudRepository<BotUser, Long> {

}
