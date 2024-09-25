plugins{
	id("com.vanniktech.dependency.graph.generator") version "0.8.0"
}

apply {
	from("gradle/dependencyGraph.gradle")
}
