package com.packt.blurApp.config;

import com.packt.blurApp.model.Role;
import com.packt.blurApp.model.User;
import com.packt.blurApp.config.security.RoleNames;
import com.packt.blurApp.repository.RoleRepository;
import com.packt.blurApp.repository.UserRepository;
import com.packt.blurApp.repository.PartyRepository;
import com.packt.blurApp.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PartyRepository partyRepository;
    private final RaceRepository raceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing application data...");
        
        // Repair schema constraints that Hibernate `ddl-auto=update` cannot remove.
        repairSchemaConstraints();

        // Initialize roles
        initializeRoles();
        
        // Create default GREAT_ADMIN user if not exists
        createDefaultAdmin();

        // Backfill existing DB rows (important when connecting to an existing Neon database)
        backfillExistingUsers();
        backfillExistingParties();
        backfillExistingRaces();

        log.info("Application data initialized successfully");
    }

    private void repairSchemaConstraints() {
        // Historically the DB had a CHECK constraint that only allowed a fixed set of role names.
        // That prevents creating custom roles and causes 409 errors like:
        //   violates check constraint "roles_name_check"
        try {
            jdbcTemplate.execute("ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_name_check");
            log.info("Schema repair: dropped constraint roles_name_check (if it existed)");
        } catch (Exception ex) {
            // Don't block app startup if the constraint doesn't exist / permissions issue.
            log.warn("Schema repair: unable to drop roles_name_check constraint: {}", ex.getMessage());
        }
    }

    private void initializeRoles() {
        log.info("Initializing roles...");
        
        // Create GREAT_ADMIN role
        if (!roleRepository.existsByName(RoleNames.GREAT_ADMIN)) {
            Role greatAdmin = Role.createGreatAdminRole();
            roleRepository.save(greatAdmin);
            log.info("Created role: GREAT_ADMIN");
        }
        
        // Only GREAT_ADMIN is seeded automatically.
        // Other roles can be managed through the admin UI.

        log.info("Roles initialized successfully");
        // Note: do not seed other roles here.
        return;
    }

    private void backfillExistingUsers() {
        log.info("Backfilling existing users (role + flags + timestamps)...");

        Role racerRole = roleRepository.findByName("RACER").orElse(null);
        if (racerRole == null) {
            // If RACER doesn't exist (because we no longer seed it), do not auto-assign it.
            // We'll only ensure admin has a role below.
            log.warn("RACER role not found; skipping auto role assignment for non-admin users");
        }
        Role greatAdminRole = roleRepository.findByName(RoleNames.GREAT_ADMIN)
                .orElseThrow(() -> new RuntimeException("GREAT_ADMIN role not found"));

        userRepository.findAll().forEach(u -> {
            boolean changed = false;

            if (u.getRole() == null) {
                // if username is admin, make it GREAT_ADMIN, else RACER
                if ("admin".equalsIgnoreCase(u.getUsername())) {
                    u.setRole(greatAdminRole);
                } else if (racerRole != null) {
                    u.setRole(racerRole);
                }
                changed = true;
            }

            if (u.getEnabled() == null) {
                u.setEnabled(true);
                changed = true;
            }
            if (u.getAccountNonExpired() == null) {
                u.setAccountNonExpired(true);
                changed = true;
            }
            if (u.getAccountNonLocked() == null) {
                u.setAccountNonLocked(true);
                changed = true;
            }
            if (u.getCredentialsNonExpired() == null) {
                u.setCredentialsNonExpired(true);
                changed = true;
            }

            if (u.getCreatedAt() == null) {
                u.setCreatedAt(java.time.LocalDateTime.now());
                changed = true;
            }
            if (u.getUpdatedAt() == null) {
                u.setUpdatedAt(java.time.LocalDateTime.now());
                changed = true;
            }

            if (changed) userRepository.save(u);
        });

        log.info("Backfill users completed");
    }

    private void backfillExistingParties() {
        log.info("Backfilling existing parties (active + dates)...");

        var parties = partyRepository.findAll();
        if (parties.isEmpty()) {
            log.info("No parties found to backfill");
            return;
        }

        // Ensure createdAt and active are set first (in-memory), without saving yet.
        for (var p : parties) {
            if (p.getActive() == null) {
                p.setActive(true);
            }
            if (p.getCreatedAt() == null) {
                p.setCreatedAt(java.time.LocalDateTime.now());
            }
        }

        // Build a set of used dates and resolve duplicates deterministically.
        java.util.Map<java.time.LocalDate, java.util.List<com.packt.blurApp.model.Party>> byDate = new java.util.HashMap<>();
        java.util.List<com.packt.blurApp.model.Party> nullDateParties = new java.util.ArrayList<>();

        for (var p : parties) {
            if (p.getPartyDate() == null) {
                nullDateParties.add(p);
            } else {
                byDate.computeIfAbsent(p.getPartyDate(), k -> new java.util.ArrayList<>()).add(p);
            }
        }

        java.util.Set<java.time.LocalDate> usedDates = new java.util.HashSet<>();

        // Handle parties that already have a date.
        // If a date appears multiple times, keep one (oldest createdAt), shift the others to unique past dates.
        for (var entry : byDate.entrySet()) {
            var date = entry.getKey();
            var list = entry.getValue();
            list.sort(java.util.Comparator.comparing(com.packt.blurApp.model.Party::getCreatedAt));

            // keep first as canonical
            usedDates.add(date);

            for (int i = 1; i < list.size(); i++) {
                var p = list.get(i);
                java.time.LocalDate candidate = date.minusDays(i);
                while (usedDates.contains(candidate)) {
                    candidate = candidate.minusDays(1);
                }
                p.setPartyDate(candidate);
                usedDates.add(candidate);
            }
        }

        // Handle parties that had null date: assign based on createdAt date; if conflict, shift backward.
        nullDateParties.sort(java.util.Comparator.comparing(com.packt.blurApp.model.Party::getCreatedAt));
        for (var p : nullDateParties) {
            java.time.LocalDate candidate = p.getCreatedAt() != null
                    ? p.getCreatedAt().toLocalDate()
                    : java.time.LocalDate.now();

            while (usedDates.contains(candidate)) {
                candidate = candidate.minusDays(1);
            }

            p.setPartyDate(candidate);
            usedDates.add(candidate);
        }

        // Persist all changes. If any constraint existed, we already ensured uniqueness.
        partyRepository.saveAll(parties);
        log.info("Backfill parties completed");
    }

    private void backfillExistingRaces() {
        log.info("Backfilling existing races (status + attributionType)...");
        raceRepository.findAll().forEach(r -> {
            boolean changed = false;
            if (r.getStatus() == null) {
                r.setStatus(com.packt.blurApp.model.enums.RaceStatus.PENDING);
                changed = true;
            }
            if (r.getAttributionType() == null) {
                r.setAttributionType(com.packt.blurApp.model.enums.AttributionType.PER_USER);
                changed = true;
            }
            if (changed) raceRepository.save(r);
        });
        log.info("Backfill races completed");
    }

    private void createDefaultAdmin() {
        log.info("Checking for default admin user...");
        
        String defaultAdminUsername = "admin";
        
        if (!userRepository.existsByUserName(defaultAdminUsername)) {
            Role greatAdminRole = roleRepository.findByName(RoleNames.GREAT_ADMIN)
                    .orElseThrow(() -> new RuntimeException("GREAT_ADMIN role not found"));
            
            User admin = User.builder()
                    .userName(defaultAdminUsername)
                    .email("admin@blurapp.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(greatAdminRole)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            
            userRepository.save(admin);
            log.info("Default admin user created: username='{}', password='admin123'", defaultAdminUsername);
            log.warn("IMPORTANT: Please change the default admin password after first login!");
        } else {
            log.info("Default admin user already exists");
        }
    }
}
