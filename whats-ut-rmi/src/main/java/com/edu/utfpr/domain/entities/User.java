package com.edu.utfpr.domain.entities;

import java.io.Serializable;

import com.edu.utfpr.client.IChatClient;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User implements Serializable {
    public String name;
    public IChatClient client;
}
