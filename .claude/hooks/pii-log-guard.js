/**
 * pii-log-guard.js — PreToolUse hook
 *
 * Blocks Write/Edit tool calls that write patient email or phone into
 * any log statement, System.out, or System.err.
 *
 * Does NOT block log statements without PII, or non-log code that happens
 * to mention email/phone in a normal identifier or comment.
 */

const PII_LOG_PATTERNS = [
  /\.log\s*\([^)]*(?:email|phone)[^)]*\)/i,
  /log\s*\.\s*(?:info|debug|warn|error|trace)\s*\([^)]*(?:email|phone)[^)]*\)/i,
  /System\s*\.\s*(?:out|err)\s*\.\s*(?:println|print)\s*\([^)]*(?:email|phone)[^)]*\)/i,
  /(?:LOG|logger|log)\s*\.\s*(?:info|debug|warn|error|trace)\s*\([^)]*(?:email|phone)[^)]*\)/gi,
];

function blockedMessage(filePath, matched) {
  return (
    "BLOCKED: Patient PII detected in log statement.\n" +
    "  File: " + filePath + "\n" +
    "  Match: " + matched + "\n" +
    "  Remove the patient email/phone from the log statement before writing."
  );
}

export function preToolUse({ toolName, arguments: args }) {
  if (toolName !== "Write" && toolName !== "Edit") {
    return;
  }

  const filePath = args.file_path || "";
  const oldString = args.old_string || "";
  const newString = args.new_string || "";
  const combined = oldString + "\n" + newString;

  for (const pattern of PII_LOG_PATTERNS) {
    pattern.lastIndex = 0;
    const match = pattern.exec(combined);
    if (match) {
      return {
        agent: {
          tip: blockedMessage(filePath, match[0]),
          block: true,
        },
      };
    }
  }

  if (toolName === "Write" && args.content) {
    for (const pattern of PII_LOG_PATTERNS) {
      pattern.lastIndex = 0;
      const match = pattern.exec(args.content);
      if (match) {
        return {
          agent: {
            tip: blockedMessage(filePath, match[0]),
            block: true,
          },
        };
      }
    }
  }
}
