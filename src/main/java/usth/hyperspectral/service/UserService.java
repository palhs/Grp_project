package usth.hyperspectral.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import usth.hyperspectral.Entity.Users;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class UserService {

    @Transactional
    public Users addUser(Users user){
        if(isUsernameDuplicate(user.getUsername())){
            return null;
        }
        user.setPassword(BcryptUtil.bcryptHash(user.getPassword()));
        user.setRole("user");
        Users.persist(user);
        return user;
    }

    public List<Users> getAllUser(){
        return Users.listAll();
    }

    public Users findUser(Long user_id){
        return Users.findById(user_id);
    }

    @Transactional
    public Users updateUser(Long user_id, Users updatedUser) {
        Users existingUser = Users.findById(user_id);

        if (existingUser != null) {
            if (Objects.nonNull(updatedUser.getPassword())) {
                existingUser.setPassword(BcryptUtil.bcryptHash(updatedUser.getPassword()));
            }
            existingUser.persist();
            return existingUser;
        } else {
            return null;
        }
    }

    @Transactional
    public boolean deleteUser(Long id){
        return Users.deleteById(id);
    }

    private boolean isUsernameDuplicate(String username) {
        Users existingUser = Users.find("username", username).firstResult();
        return existingUser != null;
    }
}