package com.viettelsoftware.firstspringboot.service.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class CurrentAuthenticatedUserNotFoundException extends RuntimeException { }
