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

package com.andrewkroh.cisco.server.directory;

/**
 * An entry in a Directory.
 *
 * @author akroh
 */
public interface DirectoryEntry
{
    /**
     * Gets the name associated with the number.
     *
     * @return name associated with the number
     */
    String getName();

    /**
     * Gets the telephone number of this directory entry.
     *
     * @return telephone number
     */
    String getTelephoneNumber();
}
