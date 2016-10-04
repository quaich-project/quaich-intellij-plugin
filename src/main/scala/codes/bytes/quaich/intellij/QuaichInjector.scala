/*
 * Copyright (c) 2016 Brendan McAdams & Thomas Lockney
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
 *
 */

package codes.bytes.quaich.intellij

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScClass, ScTypeDefinition}
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.SyntheticMembersInjector
import com.intellij.openapi.diagnostic.Logger


class QuaichInjector extends SyntheticMembersInjector {
  private val log = Logger.getInstance("codes.bytes.quaich.intellij.QuaichInjector")

  override def injectFunctions(source: ScTypeDefinition): Seq[String] = {
    log.debug(s"Inject functions on $source ; annotations: ${source.annotationNames.mkString(", ")}...")
    source match {
      case clazz: ScClass if clazz.annotationNames.contains("LambdaHTTPApi") =>
        log.info(s"Found a LambdaHTTPApi annotated class ($clazz). Returning injected functions...")
        val b = Seq.newBuilder[String]
        /**
          * Although the annotation injector tells LambdaHTTPApi classes they are instances of
          * HTTPHandler, the annotation macro also patches in a constructor that satisfies
          * the abstract members request & context.
          * Without these "fake" method defs, IntelliJ will warn you after the
          * annotation injection that you need to make your class abstract or implement.
          * Injecting fake null defs will make Intellij STFU.
          */
        b += "override def request: LambdaHTTPRequest = null" // fake it into thinking we filled the value
        b += "override def context: LambdaContext = null" // fake it into thinking we filled the value
        b.result
      case _ =>
        Seq.empty
    }
  }


  override def injectInners(source: ScTypeDefinition): Seq[String] =
    super.injectInners(source)


  override def needsCompanionObject(source: ScTypeDefinition): Boolean =
    false


  override def injectSupers(source: ScTypeDefinition): Seq[String] = {
    log.debug(s"Inject supers on $source ; annotations: ${source.annotationNames.mkString(", ")}...")
    source match {
      case clazz: ScClass if clazz.annotationNames.contains("LambdaHTTPApi") =>
        log.info("Found a LambdaHTTPApi annotated class. Returning the HTTPHandler trait as a super...")
        Seq("codes.bytes.quaich.api.http.routing.HTTPHandler")
      case _ =>
        Seq.empty
    }
  }

}

// vim: set ts=2 sw=2 sts=2 et:
