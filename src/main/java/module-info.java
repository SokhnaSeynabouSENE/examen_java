module com.xoslu.tech.chat_demo_app {
    requires javafx.controls;
    requires javafx.fxml;

// Hibernate/JPA
    requires org.hibernate.orm.core;

// PostgreSQL JDBC
    requires java.sql;

// Pour la sérialisation réseau (inclus par défaut)
    requires java.persistence;
    requires static lombok;

// Ouvre les packages pour JavaFX et Hibernate (réflexion)
    opens client.controller to javafx.fxml;
    opens client.model to javafx.base;
    opens server.model to org.hibernate.orm.core, javax.persistence;
    opens server.dao to org.hibernate.orm.core, javax.persistence;

// Exporte les packages (si besoin)
    exports client;
    exports client.controller;
    exports server;
}