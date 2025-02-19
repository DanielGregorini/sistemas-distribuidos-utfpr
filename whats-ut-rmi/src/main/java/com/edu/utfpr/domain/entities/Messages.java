package com.edu.utfpr.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Messages implements Serializable {
    public User sender;
    public String content;
}
