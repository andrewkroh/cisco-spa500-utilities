package com.andrewkroh.cisco.common;

/**
 * Pluggable exception handler that allows users to customize
 * exception handling within a component.
 *
 * @author akroh
 */
public interface ExceptionHandler
{
    void handle(Exception e, String errorMessage);
}
