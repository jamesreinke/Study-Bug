package controllers

import sts._
import awsscala._


object Amazon {

	implicit val sts = STS()

	val federation: FederationToken = sts.federationToken(
		name = "anonymous-user",
		policy = Policy(Seq(Statement(Effect.Allow, Seq(Action("s3:*")), Seq(Resource("*"))))),
		durationSeconds = 1200)

	val signinToken: String = sts.signinToken(federation.credentials)

	val loginUrl: String = sts.loginUrl(
		credentials = federation.credentials,
		consoleUrl = "https://console.aws.amazon.com/iam",
		issueUrl = "http://example.com/internal/auth")

}