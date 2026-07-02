# Configure Any Project for spring-mcp Code Review

Use this guide to set up a **consumer project** so Cursor agents always consult **spring-mcp** rules **before** general code review findings — without copying rule text into the project and without mentioning spring-mcp in every prompt.

When you need to configure a new repo, reference this file in Cursor (for example: `@configure-project-mcp-review.md configure this project`).

---

## What you get after setup

When someone asks for a code review in the configured project, the agent should:

1. Read the spring-mcp MCP tool schema
2. Call the `pattern` tool
3. Report **MCP rule violations first**
4. Then add general code quality findings
5. **Not** paste spring-mcp spec content into the consumer repo

---

## Prerequisites

Complete these once on your machine (or verify they are already done).

### 1. Build spring-mcp

From the spring-mcp repository root:

```cmd
cd C:\Users\User\github\spring-mcp
mvnw.cmd clean package
```

Confirm the JAR exists:

```
C:\Users\User\github\spring-mcp\target\spring-mcp-0.0.1-SNAPSHOT.jar
```

> If the build fails because the JAR is locked, stop running MCP server instances first (Cursor keeps the JAR open while connected).

### 2. Know your absolute paths

Replace placeholders in this guide with your real paths:

| Placeholder | Example (this machine) |
|-------------|-------------------------|
| `{SPRING_MCP_JAR}` | `C:/Users/User/github/spring-mcp/target/spring-mcp-0.0.1-SNAPSHOT.jar` |
| `{SPECS_PATH}` | `C:/Users/User/github/spring-mcp/src/main/resources/specs/pattern` |
| `{TARGET_PROJECT}` | `C:/Users/User/github/java15` |

Use forward slashes in JSON paths on Windows.

### 3. Java 17+

spring-mcp requires Java 17 or newer:

```cmd
java -version
```

---

## Per-project setup (recommended for teams)

Add two files to the **consumer project** (the repo you want reviewed).

### Step 1 — Create `.cursor/mcp.json`

In `{TARGET_PROJECT}/.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "spring-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "{SPRING_MCP_JAR}",
        "--mcp.pattern.specs-path={SPECS_PATH}"
      ]
    }
  }
}
```

**Example for java15:**

```json
{
  "mcpServers": {
    "spring-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "C:/Users/User/github/spring-mcp/target/spring-mcp-0.0.1-SNAPSHOT.jar",
        "--mcp.pattern.specs-path=C:/Users/User/github/spring-mcp/src/main/resources/specs/pattern"
      ]
    }
  }
}
```

This registers spring-mcp for that project only.

### Step 2 — Create `.cursor/rules/code-review-spring-mcp.mdc`

In `{TARGET_PROJECT}/.cursor/rules/code-review-spring-mcp.mdc`:

```markdown
---
description: Use spring-mcp MCP server for all code reviews
alwaysApply: true
---

# Code Review — spring-mcp Integration

When performing any code review (explicit request, PR review, or change audit):

1. **Consult spring-mcp first** — use the `spring-mcp` MCP server before finalizing findings.
2. **Read tool schemas** — list and read MCP tool descriptors before invoking any tool.
3. **Fetch rules at review time** — call available review tools (e.g. `pattern`) and treat MCP responses as the authoritative rule source.
4. **Do not copy rules** — never duplicate spring-mcp spec text into this repo (README, rules, docs, or review files). Reference MCP-derived findings only in the review output.
5. **Apply and report** — validate code against fetched specs; cite which MCP tool flagged each issue.
6. **Handle MCP failures** — if a tool is unavailable or errors, state that in the review and continue with general review after the MCP attempt.

Review output order: MCP rule violations → general code quality → summary and priorities.
```

`alwaysApply: true` ensures the agent receives this workflow on every session in that project.

### Step 3 — Final project layout

```
{TARGET_PROJECT}/
├── .cursor/
│   ├── mcp.json
│   └── rules/
│       └── code-review-spring-mcp.mdc
├── src/
└── ...
```

### Step 4 — Commit (optional but recommended for teams)

```cmd
cd {TARGET_PROJECT}
git add .cursor/mcp.json .cursor/rules/code-review-spring-mcp.mdc
git commit -m "Configure spring-mcp as primary code review rule source"
```

---

## Global setup (optional — all projects on your machine)

Use this **instead of** or **in addition to** per-project files.

### Global MCP server

File: `C:\Users\User\.cursor\mcp.json`

```json
{
  "mcpServers": {
    "spring-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "C:/Users/User/github/spring-mcp/target/spring-mcp-0.0.1-SNAPSHOT.jar",
        "--mcp.pattern.specs-path=C:/Users/User/github/spring-mcp/src/main/resources/specs/pattern"
      ]
    }
  }
}
```

Also configurable in **Cursor Settings → MCP**.

### Global review rule

Add the same rule content from Step 2 as a **User Rule** in **Cursor Settings → Rules**, or create a personal skill at `~/.cursor/skills/spring-mcp-review/SKILL.md`.

| Approach | Scope | Reliability |
|----------|-------|-------------|
| Per-project `.mdc` rule with `alwaysApply: true` | One repo / team | Highest for that repo |
| User Rule (global) | All your projects | High |
| Personal Skill | All your projects | Medium (intent-based trigger) |

**Recommendation:** per-project rule for shared repos; global User Rule if you want every project covered without adding files.

---

## Verify the setup

### 1. Restart or reload Cursor

After adding `.cursor/mcp.json`, restart Cursor or reload the window so the MCP server starts.

### 2. Confirm MCP server is running

In **Cursor Settings → MCP**, verify `spring-mcp` shows as connected (no errors).

### 3. Test the `pattern` tool manually

In MCP Inspector or by asking the agent to call it:

- Server: `user-spring-mcp` (or `spring-mcp`)
- Tool: `pattern`
- Expected: content from `variablenames.md`

Current rule example:

```markdown
* boolean variables must start with is*, has* or should* prefix
```

### 4. Run a code review smoke test

In the configured project, ask:

> Review this project as a senior developer.

Expected agent behavior:

1. Calls `pattern` before finalizing findings
2. Flags violations such as `boolean name` (invalid — must be `is*`, `has*`, or `should*`)
3. Reports MCP violations before general findings

---

## How to use this guide later

When configuring a new project, open Cursor in that repo and reference this file:

```
@configure-project-mcp-review.md set up spring-mcp review for this project
```

Or paste the target project path:

```
@configure-project-mcp-review.md configure C:/Users/User/github/my-new-project
```

The agent should create `.cursor/mcp.json` and `.cursor/rules/code-review-spring-mcp.mdc` using the paths from this guide.

---

## Updating review rules (no consumer changes)

Rules live in **spring-mcp**, not in consumer repos.

1. Edit or add specs under:
   ```
   spring-mcp/src/main/resources/specs/pattern/
   ```
2. Expose new specs via `@McpTool` in `Pattern.java` (if adding new files or tools).
3. Rebuild:
   ```cmd
   cd C:\Users\User\github\spring-mcp
   mvnw.cmd clean package
   ```
4. Restart the MCP server (reload Cursor or reconnect MCP).

Consumer projects pick up new rules automatically on the next review.

---

## Troubleshooting

### `pattern` tool fails — file not found

**Symptom:**

```
File not found: C:\Users\User\src\main\resources\specs\pattern\variablenames.md
```

**Cause:** `mcp.pattern.specs-path` is relative and resolves from the wrong working directory.

**Fix:** Use an **absolute path** in `mcp.json`:

```json
"--mcp.pattern.specs-path=C:/Users/User/github/spring-mcp/src/main/resources/specs/pattern"
```

### Agent does not call spring-mcp

**Cause:** MCP is enabled but no rule instructs the agent to use it.

**Fix:** Ensure `.cursor/rules/code-review-spring-mcp.mdc` exists with `alwaysApply: true`, or add a global User Rule.

### JAR build fails — file locked

**Cause:** Cursor or MCP Inspector has the JAR open.

**Fix:** Disconnect spring-mcp in Cursor MCP settings, then rebuild.

### MCP server not listed in project

**Cause:** Missing or invalid `.cursor/mcp.json`.

**Fix:** Create the file at the project root (not inside `src/`). Validate JSON syntax.

### Review runs but ignores boolean naming rule

**Cause:** Agent called MCP after already writing findings, or MCP call failed silently.

**Fix:** Confirm `pattern` returns specs manually. Ensure the rule says **consult spring-mcp first**.

---

## Quick copy checklist

Use this when onboarding a new project:

```
[ ] spring-mcp JAR built (mvnw.cmd clean package)
[ ] Absolute paths known for JAR and specs directory
[ ] {PROJECT}/.cursor/mcp.json created
[ ] {PROJECT}/.cursor/rules/code-review-spring-mcp.mdc created
[ ] Cursor reloaded
[ ] spring-mcp shows connected in MCP settings
[ ] pattern tool returns variablenames.md
[ ] Smoke test review flags MCP violations first
[ ] (Optional) .cursor files committed to git
```

---

## Related files in spring-mcp

| File | Purpose |
|------|---------|
| `src/main/resources/specs/pattern/variablenames.md` | Current review rules |
| `src/main/java/com/mcp/context/Pattern.java` | `pattern` MCP tool |
| `src/main/java/com/mcp/service/file/FileService.java` | Reads spec files safely |
| `README.md` | spring-mcp architecture and development guide |
