/*
 * Copyright 2013 Andrew Kroh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andrewkroh.cisco.server.phone.rest;

/**
 * Interface for any JAX-RS phone application that wants to be automatically
 * published at runtime.
 *
 * @author akroh
 */
public interface PhoneApplication
{
    /**
     * The ID of the application. This ID will be used to access the
     * application. The URL will be {@code .../rest/app/[applicationId]}.
     *
     * @return ID of the application
     */
    String getApplicationId();
}
