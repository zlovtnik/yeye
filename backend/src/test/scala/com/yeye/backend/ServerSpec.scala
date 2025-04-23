package com.yeye.backend

import zio.*
import zio.http.*
import zio.test.{
  ZIOSpecDefault,
  assert,
  equalTo,
  isNonEmptyString,
  matchesRegex,
  suite,
  test
}
import zio.json.*
import caliban.GraphQLRequest

object ServerSpec extends ZIOSpecDefault:
  def spec = suite("Server")(
    test("GraphQL endpoint should return valid JSON response") {
      for
        client <- ZIO.service[Client]
        request = Request
          .post(
            URL.decode("http://localhost:8080/api/graphql").toOption.get,
            Body.fromString(
              """{
                |  "query": "{ __schema { types { name } } }"
                |}""".stripMargin
            )
          )
          .addHeader(Header.ContentType(MediaType.application.json))
        response <- client.request(request)
        body <- response.body.asString
        _ <- ZIO.debug(s"Response status: ${response.status}")
        _ <- ZIO.debug(s"Response body: $body")
      yield assert(response.status)(equalTo(Status.Ok)) &&
        assert(body)(isNonEmptyString) &&
        assert(body)(matchesRegex("\\{.*\\}"))
    },
    test("GraphQL endpoint should handle invalid requests gracefully") {
      for
        client <- ZIO.service[Client]
        request = Request
          .post(
            URL.decode("http://localhost:8080/api/graphql").toOption.get,
            Body.fromString("invalid json")
          )
          .addHeader(Header.ContentType(MediaType.application.json))
        response <- client.request(request)
        body <- response.body.asString
      yield assert(response.status)(equalTo(Status.BadRequest)) &&
        assert(body)(isNonEmptyString)
    }
  ).provide(
    Client.default,
    Server.app.toHttpApp @@ Middleware.cors(Server.corsConfig)
  )
