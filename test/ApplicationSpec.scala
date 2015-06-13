import org.scalatest.WordSpec

import play.api.test._
import play.api.test.Helpers._

class Answers extends WordSpec {

  import models.problems.Answer._
  import models.problems.Answer

  "Model" when {
    val a = new Answer(1, "contents", "picture", 1, true)
    val b = new Answer(1, "contents", "picture", 1, true)
    val c = new Answer(1, "contents", "picture", 1, false)
    "inserting 3 distinct rows" should {
      "have size 3" in new WithApplication {
        create(a)
        create(b)
        create(c)
        val d = getByProblemId(a.problemId)
        assert(d.length == 3)
      }
    }
    "deleting 1 of 3 rows" should {
      "have size 2" in new WithApplication {
        create(a)
        create(b)
        create(c)
        delete(a)
        val d = getByProblemId(a.problemId)
        assert(d.length == 2)
      }
    }
  }
}

class Solutions extends WordSpec {

  import models.problems.Subtopic
  import models.problems.SolutionStep
  import models.problems.Solution._

  "Model" when {
    val s = new Subtopic(1, "contents", "hint")
    val a = new SolutionStep(1, "contents", "picture", 1, 1, List(s))
    val b = new SolutionStep(1, "contents", "picture", 1, 2, List(s))
    val c = new SolutionStep(1, "contents", "picture", 1, 3, List(s))
    "inserting 3 distinct rows" should {
      "have size 3" in new WithApplication {
        models.problems.Subtopic.create(s)
        create(a)
        create(b)
        create(c)
        val d = getByProblemId(a.problemId)
        assert(d.length == 3)
      }
    }
    "deleting 1 of 3 rows" should {
      "have size 2" in new WithApplication {
        create(a)
        create(b)
        create(c)
        delete(a)
        val d = getByProblemId(a.problemId)
        assert(d.length == 2)
      }
    }
  }
}

class Subtopics extends WordSpec {

  import models.problems.Subtopic
  import models.problems.Subtopic._

  "Model" when {
    val a = new Subtopic(1, "contents", "hint")
    val b = new Subtopic(1, "different contents", "hint")
    val c = new Subtopic(1, "contents", "hint")
    "inserting 3 rows (2 distinct)" should {
      "have size 2" in new WithApplication {
        create(a)
        create(b)
        create(c)
        val d = getAll
        assert(d.length == 2)
      }
    }
    "inserting 3 rows (2 distinct) with 1 deletion" should {
      "have size 1" in new WithApplication {
        create(a)
        create(b)
        create(c)
        delete(a)
        assert(getAll.length == 1)
      }
    }
    "inserting 1 row and checking if same item exists" should {
      "return true" in new WithApplication {
        create(a)
        assert(exists(a))
      }
    }
  }
}

class Topics extends WordSpec {

  import models.problems.Topic
  import models.problems.Topic._

  "Model" when {
    val a = new Topic(1, "root", 0)
    val b = new Topic(1, "sub-root", 1)
    val c = new Topic(1, "sub-sub-root", 2)
    val x = new Topic(1, "parent", 0)
    val y = new Topic(1, "child one", 1)
    val z = new Topic(1, "child two", 1)
    "inserting 3 distinct rows" should {
      "have size 3" in new WithApplication {
        create(a)
        create(b)
        create(c)
        assert(getAll.length == 3)
      }
    }
    "inserting 3 rows such that root->child->leaf" should {
      "return 2 parents when asking for the parents of the leaf" in new WithApplication {
        create(a)
        create(b)
        create(c)
        assert(getParents(c).length == 2)
      }
    }
    "insert 3 topics, two of which are children of the parent" should {
      "return 2 children when asking for the roots" in new WithApplication {
        create(x)
        create(y)
        create(z)
        assert(getChildren(x).length == 2)
      }
    }
  }
}

class Authentication extends WordSpec {

  import models.users.{User, Login, Register}
  import models.users.Authentication._

  "Model" when {
    val a = new User(1, "jamesreinke91@gmail.com", "t00thbrush", true)
    val b = new User(1, "james@rtsystems.io", "t00thbrusht00thbrush", false)
    val c = new User(1, "jamesreinke91@gmail.com", "anotherpassword", false)
    val d = new Login("jamesreinke91@gmail.com", "t00thbrush")
    val e = new Login("jamesreinke91@gmail.com", "brusht00th")
    "inserting 3 rows (2 distinct)" should {
      "have size 2" in new WithApplication {
        create(a)
        create(b)
        create(c)
        (getAll.length == 2)
      }
    }
    "inserting 1 user and supplying the correct password" should {
      "return the user" in new WithApplication {
        create(a)
        assert(login(d) != None)
      }
    }
    "inserting 1 user and supplying an incrrect password" should {
      "return None" in new WithApplication {
        create(a)
        assert(login(e) == None)
      }
    }
  }
}

