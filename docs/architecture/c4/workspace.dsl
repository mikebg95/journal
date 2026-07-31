workspace "Journal" "A personal administrative AI-generated tool to organize journal entries." {
    model {
        user = person "User" "A person who inserts, edits, deletes and browses their personal journal entries."

        journal = softwareSystem "Journal" "Allows users to create, browse, and manage their journal entries. Also generates information about entries (summary, tags, mood) based on content." {
            frontend = container "Frontend Web Application" "Provides the User Interface for creating, browsing, editing, and deleting journal entries." "Angular"
            backend = container "Backend API" "Handles entry CRUD operations and generates relevant information about journal entries." "Java, Spring Boot, Spring AI" {
                web = component "Web Adapter" "Is the entry point to the backend API" "Spring MVC"
                domain = component "Core Domain" "Business logic for entries and tags; defines the ports the adapters implement" "Plain Java"
                persistence = component "Persistence Adapter" "Provides data access for journal entries, tags, and moods." "Spring Data JPA"
                ai = component "AI Adapter" "Implements the enricher port; calls the external LLM and translates its response" "Spring AI"
            } 
            database = container "Database" "Stores journal entries, tags, and moods." "PostgreSQL" "Database"
        }

        llm = softwareSystem "LLM Model" "Analyses journal entry content and generates relevant summary, tags, and mood." "External"

        user -> frontend "Browses, creates, edits, deletes journal entries" "HTTPS"
        frontend -> web "Makes API calls to" "JSON/HTTPS"
        web -> domain "Delegates entry operations to"
        domain -> ai "Uses to analyze entry content and generate relevant information"
        ai -> llm "Sends entry content to, receives analysis from" "HTTPS"
        domain -> persistence "Uses to persist and retrieve data"
        persistence -> database "Reads from and writes to" "JDBC"
    }

    views {
        systemContext journal "SystemContext" {
            include *
            autolayout lr
        }

        container journal "Containers" {
            include *
            autolayout lr
        }

        component backend "Component" {
            include *
            autolayout lr
        }

        styles {
            element "Person" {
                background #08427b
                color #ffffff
                shape Person
            }
            element "Software System" {
                background #1168bd
                color #ffffff 
            }    
            element "Container" {
                background #438dd5
                color #ffffff
            }
            element "Component" {
                background #85bbf0
                color #000000
            }
            element "Database" {
                shape Cylinder
            }
            element "External" {
                background #999999
                color #ffffff
            }
        }
    }
}