export function formatSocialContacts(contacts: Record<string, string>): string {
  return JSON.stringify(contacts ?? {}, null, 2);
}

export function parseSocialContacts(value: string): Record<string, string> {
  const trimmed = value.trim();
  if (!trimmed) {
    return {};
  }

  try {
    const parsed = JSON.parse(trimmed) as Record<string, string>;
    return Object.entries(parsed).reduce<Record<string, string>>((result, [key, contact]) => {
      if (key.trim() && typeof contact === 'string' && contact.trim()) {
        result[key.trim()] = contact.trim();
      }
      return result;
    }, {});
  } catch {
    return {};
  }
}
