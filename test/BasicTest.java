import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;
import java.util.List;

public class BasicTest extends UnitTest {

	@Before
	public void setup() {
		Fixtures.deleteDatabase();
	}

    @Test
    public void createAndRetrieveUser() {
		new User("martin@hot.ee", "secret", "Martin").save();
		User martin = User.find("byEmail", "martin@hot.ee").first();
		
		assertNotNull(martin);
		assertEquals("Martin", martin.fullname);
	}
	
	@Test
	public void tryConnectAsUser() {
		new User("martin@hot.ee", "secret", "Martin").save();
		
		assertNotNull(User.connect("martin@hot.ee", "secret"));
		assertNull(User.connect("martin@hot.ee", "badpassword"));
		assertNull(User.connect("renar@hot.ee", "secret"));
	}
	
	@Test
	public void createPost() {

		User martin = new User("martin@hot.ee", "secret", "Martin").save();

		new Post(martin, "My first post", "Hello world").save();

		assertEquals(1, Post.count());

		List<Post> martinPosts = Post.find("byAuthor", martin).fetch();
		
		assertEquals(1, martinPosts.size());
		Post firstPost = martinPosts.get(0);
		assertNotNull(firstPost);
		assertEquals(martin, firstPost.author);
		assertEquals("My first post", firstPost.title);
		assertEquals("Hello world", firstPost.content);
		assertNotNull(firstPost.postedAt);
	}
	
	@Test
	public void postComments() {
		User martin = new User("martin@hot.ee", "secret", "Martin").save();

		Post martinPost = new Post(martin, "My first post", "Hello world").save();

		new Comment(martinPost, "Jeff", "Nice post").save();
		new Comment(martinPost, "Tom", "I knew that !").save();

		List<Comment> martinPostComments = Comment.find("byPost", martinPost).fetch();
	 
		assertEquals(2, martinPostComments.size());
	 
		Comment firstComment = martinPostComments.get(0);
		assertNotNull(firstComment);
		assertEquals("Jeff", firstComment.author);
		assertEquals("Nice post", firstComment.content);
		assertNotNull(firstComment.postedAt);
	 
		Comment secondComment = martinPostComments.get(1);
		assertNotNull(secondComment);
		assertEquals("Tom", secondComment.author);
		assertEquals("I knew that !", secondComment.content);
		assertNotNull(secondComment.postedAt);
	}
	
	@Test
	public void useTheCommentsRelation() {
		User martin = new User("martin@hot.ee", "secret", "Martin").save();

		Post martinPost = new Post(martin, "My first post", "Hello world").save();

		martinPost.addComment("Jeff", "Nice post");
		martinPost.addComment("Tom", "I knew that !");

		assertEquals(1, User.count());
		assertEquals(1, Post.count());
		assertEquals(2, Comment.count());

		martinPost = Post.find("byAuthor", martin).first();
		assertNotNull(martinPost);

		assertEquals(2, martinPost.comments.size());
		assertEquals("Jeff", martinPost.comments.get(0).author);

		martinPost.delete();

		assertEquals(1, User.count());
		assertEquals(0, Post.count());
		assertEquals(0, Comment.count());
	}
	
	@Test
	public void fullTest() {
		Fixtures.loadModels("data.yml");

		assertEquals(2, User.count());
		assertEquals(3, Post.count());
		assertEquals(3, Comment.count());

		assertNotNull(User.connect("bob@gmail.com", "secret"));
		assertNotNull(User.connect("jeff@gmail.com", "secret"));
		assertNull(User.connect("jeff@gmail.com", "badpassword"));
		assertNull(User.connect("tom@gmail.com", "secret"));

		List<Post> bobPosts = Post.find("author.email", "bob@gmail.com").fetch();
		assertEquals(2, bobPosts.size());

		List<Comment> bobComments = Comment.find("post.author.email", "bob@gmail.com").fetch();
		assertEquals(3, bobComments.size());

		Post frontPost = Post.find("order by postedAt desc").first();
		assertNotNull(frontPost);
		assertEquals("About the model layer", frontPost.title);

		assertEquals(2, frontPost.comments.size());

		frontPost.addComment("Jim", "Hello guys");
		assertEquals(3, frontPost.comments.size());
		assertEquals(4, Comment.count());
	}
	
	@Test
	public void testTags() {
		// Create a new user and save it
		User bob = new User("bob@gmail.com", "secret", "Bob").save();
	 
		// Create a new post
		Post bobPost = new Post(bob, "My first post", "Hello world").save();
		Post anotherBobPost = new Post(bob, "Hop", "Hello world").save();
	 
		// Well
		assertEquals(0, Post.findTaggedWith("Red").size());
	 
		// Tag it now
		bobPost.tagItWith("Red").tagItWith("Blue").save();
		anotherBobPost.tagItWith("Red").tagItWith("Green").save();
	 
		// Check
		assertEquals(2, Post.findTaggedWith("Red").size());
		assertEquals(1, Post.findTaggedWith("Blue").size());
		assertEquals(1, Post.findTaggedWith("Green").size());
		assertEquals(1, Post.findTaggedWith("Red", "Blue").size());
		assertEquals(1, Post.findTaggedWith("Red", "Green").size());
		assertEquals(0, Post.findTaggedWith("Red", "Green", "Blue").size());
		assertEquals(0, Post.findTaggedWith("Green", "Blue").size());
		List<Map> cloud = Tag.getCloud();
		assertEquals(
			"[{tag=Blue, pound=1}, {tag=Green, pound=1}, {tag=Red, pound=2}]",
			cloud.toString()
		);
	}
}
