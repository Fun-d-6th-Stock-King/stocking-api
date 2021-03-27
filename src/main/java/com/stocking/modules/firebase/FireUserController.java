package com.stocking.modules.firebase;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequestMapping(value = "/api/fireuser")
@RestController
@RequiredArgsConstructor
public class FireUserController {

    private final FireUserService fireUserService;

    @PostMapping
    public ResponseEntity<Object> saveUser(
        @RequestBody FireUserReq fireUserReq
    ) {
        return new ResponseEntity<>(
            fireUserService.save(fireUserReq)
        , HttpStatus.OK);
    }
    
    @GetMapping
    public ResponseEntity<Object> findUserList(
    ) {
        return new ResponseEntity<>(
            fireUserService.getList()
        , HttpStatus.OK);
    }
    
    @GetMapping("/{uid}")
    public ResponseEntity<Object> findUser(
        @PathVariable String uid
    ) throws NotFoundException {
        return new ResponseEntity<>(
            fireUserService.getFireUser(uid)
        , HttpStatus.OK);
    }
}
