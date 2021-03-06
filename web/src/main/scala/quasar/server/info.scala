/*
 * Copyright 2014–2016 SlamData Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package quasar.server

import quasar.Predef._
import quasar.build.BuildInfo

import argonaut._, Argonaut._
import org.http4s.dsl._
import org.http4s.argonaut._
import org.http4s.HttpService

object info {
  val nameAndVersionInfo = Json("name" := "Quasar", "version" := BuildInfo.version)

  val service: HttpService = HttpService {
    case GET -> Root => Ok(nameAndVersionInfo)
  }
}
