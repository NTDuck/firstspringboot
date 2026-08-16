package com.viettelsoftware.firstspringboot.service;

import com.viettelsoftware.firstspringboot.entity.User;
import lombok.NonNull;

import java.util.List;

public interface UserExportService {
    byte[] exportUsers(List<@NonNull User> users);
}
