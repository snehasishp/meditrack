# Testing Rules

## Stack

- JUnit 5
- Mockito
- AssertJ (always `assertThat(...)`)
- MockMvc for controller/integration tests

Never use `assertEquals` — use AssertJ assertions exclusively.

## Test Structure

```
src/test/java/com/bharath/meditrack/
  controller/     MockMvc tests (full Spring context or @WebMvcTest slice)
  service/        Unit tests for @Service classes
  repo/           Integration tests with @DataJpaTest
```

## Naming

- `*ServiceTest.java` — service layer unit tests
- `*ControllerTest.java` — MockMvc tests
- `*RepositoryTest.java` — @DataJpaTest integration tests

Test method names must match `^test[A-Z][a-zA-Z0-9]*$`:

```java
@Test
void testBookAppointmentSuccess() { ... }

@Test
void testBookAppointmentPatientNotFound() { ... }

@Test
void testCancelAppointmentNoSlotsAvailable() { ... }
```

The prefix `test` + capitalised first letter + descriptive camelCase body is required on every `@Test` method. No underscores, no `should_`, no bare `testXxx`. Every segment after `test` is capitalised camelCase.

## Mandatory Coverage

Every new `@Service` class must have a corresponding `*ServiceTest.java` with:

- Happy-path test for each public method
- Error/exception path for each throwing case
- All business rules exercised (e.g. slot capacity check, status transitions)

## MockMvc Test Pattern

```java
@WebMvcTest(SomeController.class)
class SomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SomeService someService;

    @Test
    void testGetSomethingReturnsOk() throws Exception {
        when(someService.findById(1L)).thenReturn(someDto);

        mockMvc.perform(get("/api/v1/something/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetSomethingNotFoundReturns404() throws Exception {
        when(someService.findById(999L)).thenThrow(new EntityNotFoundException(...));

        mockMvc.perform(get("/api/v1/something/999"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.message").exists());
    }
}
```

## AssertJ Usage

```java
// Always
assertThat(result).isNotNull();
assertThat(result.getId()).isEqualTo(1L);
assertThat(list).hasSize(3);
assertThatThrownBy(() -> service.book(...)).isInstanceOf(...)
```

## Dependency

Add `spring-boot-starter-test` to `pom.xml` before writing tests. It is absent in the starter.
