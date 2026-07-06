import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Loader2, Save, UserPlus, Users } from 'lucide-react';
import { AdminUserPayload } from '../../context/AuthContext';
import { Session } from '../../types/Session';
import { UserResponse } from '../../types/UserResponse';
import { formatSocialContacts, parseSocialContacts } from '../../utils/socialContacts';
import styles from './AdminUsersPanel.module.css';

type AdminUsersPanelProps = {
  session: Session | null;
  busy: boolean;
  isAdmin: boolean;
  users: UserResponse[];
  onLoadUsers: () => Promise<void>;
  onCreateUser: (payload: AdminUserPayload) => Promise<void>;
  onUpdateUser: (id: string, payload: AdminUserPayload) => Promise<void>;
};

type Draft = {
  username: string;
  email: string;
  fullName: string;
  password: string;
  roles: string;
  enabled: boolean;
  phoneNumber: string;
  birthday: string;
  position: string;
  profilePictureUrl: string;
  socialContactsJson: string;
};

function emptyDraft(): Draft {
  return {
    username: '',
    email: '',
    fullName: '',
    password: '',
    roles: 'ROLE_USER',
    enabled: true,
    phoneNumber: '',
    birthday: '',
    position: '',
    profilePictureUrl: '',
    socialContactsJson: '{}',
  };
}

function fromUser(user: UserResponse): Draft {
  return {
    username: user.username,
    email: user.email,
    fullName: user.fullName ?? '',
    password: '',
    roles: user.roles.join(', '),
    enabled: user.enabled,
    phoneNumber: user.phoneNumber ?? '',
    birthday: user.birthday ?? '',
    position: user.position ?? '',
    profilePictureUrl: user.profilePictureUrl ?? '',
    socialContactsJson: formatSocialContacts(user.socialContacts ?? {}),
  };
}

function toPayload(draft: Draft): AdminUserPayload {
  return {
    username: draft.username.trim(),
    email: draft.email.trim(),
    fullName: draft.fullName.trim(),
    password: draft.password ? draft.password : undefined,
    roles: draft.roles
      .split(',')
      .map((role) => role.trim())
      .filter(Boolean),
    enabled: draft.enabled,
    phoneNumber: draft.phoneNumber.trim(),
    birthday: draft.birthday,
    position: draft.position.trim(),
    profilePictureUrl: draft.profilePictureUrl.trim(),
    socialContacts: parseSocialContacts(draft.socialContactsJson),
  };
}

export function AdminUsersPanel({ session, busy, isAdmin, users, onLoadUsers, onCreateUser, onUpdateUser }: AdminUsersPanelProps) {
  const [createDraft, setCreateDraft] = useState<Draft>(emptyDraft());
  const [editingId, setEditingId] = useState<string | null>(null);
  const [drafts, setDrafts] = useState<Record<string, Draft>>({});

  const editingDraft = useMemo(() => (editingId ? drafts[editingId] : null), [drafts, editingId]);

  useEffect(() => {
    if (!editingId || !users.length) {
      return;
    }

    const matched = users.find((user) => user.id === editingId);
    if (!matched) {
      setEditingId(null);
      return;
    }

    setDrafts((current) => ({
      ...current,
      [matched.id]: current[matched.id] ?? fromUser(matched),
    }));
  }, [editingId, users]);

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      await onCreateUser(toPayload(createDraft));
      setCreateDraft(emptyDraft());
    } catch {
      return;
    }
  }

  async function handleUpdate(event: FormEvent<HTMLFormElement>, id: string) {
    event.preventDefault();
    const draft = drafts[id];
    if (!draft) {
      return;
    }

    try {
      await onUpdateUser(id, toPayload(draft));
      setEditingId(null);
    } catch {
      return;
    }
  }

  function beginEdit(user: UserResponse) {
    setEditingId(user.id);
    setDrafts((current) => ({
      ...current,
      [user.id]: fromUser(user),
    }));
  }

  return (
    <section className={`${styles.panel} ${styles.usersPanel}`} id="users">
      <div className={styles.panelHeading}>
        <div>
          <p className={styles.eyebrow}>Users</p>
          <h2>Administration</h2>
        </div>
        <button className={styles.iconButton} type="button" onClick={onLoadUsers} disabled={!session || busy} title="Load users">
          {busy ? <Loader2 className={styles.spin} size={18} /> : <Users size={18} />}
        </button>
      </div>

      {!isAdmin && <p className={styles.emptyState}>Admin access is required to manage users.</p>}

      {isAdmin && (
        <>
          <form className={styles.createForm} onSubmit={handleCreate}>
            <div className={styles.subheading}>
              <UserPlus size={16} />
              <h3>Create new profile</h3>
            </div>

            <div className={styles.grid}>
              <label className={styles.field}>
                <span>Username</span>
                <input value={createDraft.username} onChange={(event) => setCreateDraft({ ...createDraft, username: event.target.value })} required />
              </label>
              <label className={styles.field}>
                <span>Email</span>
                <input value={createDraft.email} onChange={(event) => setCreateDraft({ ...createDraft, email: event.target.value })} type="email" required />
              </label>
              <label className={styles.field}>
                <span>Full name</span>
                <input value={createDraft.fullName} onChange={(event) => setCreateDraft({ ...createDraft, fullName: event.target.value })} />
              </label>
              <label className={styles.field}>
                <span>Password</span>
                <input value={createDraft.password} onChange={(event) => setCreateDraft({ ...createDraft, password: event.target.value })} type="password" minLength={8} required />
              </label>
              <label className={styles.field}>
                <span>Roles</span>
                <input value={createDraft.roles} onChange={(event) => setCreateDraft({ ...createDraft, roles: event.target.value })} placeholder="ROLE_USER, ROLE_ADMIN" required />
              </label>
              <label className={styles.field}>
                <span>Phone number</span>
                <input value={createDraft.phoneNumber} onChange={(event) => setCreateDraft({ ...createDraft, phoneNumber: event.target.value })} />
              </label>
              <label className={styles.field}>
                <span>Birthday</span>
                <input value={createDraft.birthday} onChange={(event) => setCreateDraft({ ...createDraft, birthday: event.target.value })} type="date" />
              </label>
              <label className={styles.field}>
                <span>Position</span>
                <input value={createDraft.position} onChange={(event) => setCreateDraft({ ...createDraft, position: event.target.value })} />
              </label>
              <label className={styles.field}>
                <span>Profile picture URL</span>
                <input value={createDraft.profilePictureUrl} onChange={(event) => setCreateDraft({ ...createDraft, profilePictureUrl: event.target.value })} />
              </label>
              <label className={styles.fieldFull}>
                <span>Social contacts JSON</span>
                <textarea rows={4} value={createDraft.socialContactsJson} onChange={(event) => setCreateDraft({ ...createDraft, socialContactsJson: event.target.value })} />
              </label>
              <label className={styles.checkbox}>
                <input
                  checked={createDraft.enabled}
                  onChange={(event) => setCreateDraft({ ...createDraft, enabled: event.target.checked })}
                  type="checkbox"
                />
                <span>Enabled</span>
              </label>
            </div>

            <button className={styles.primaryAction} type="submit" disabled={busy}>
              <Save size={18} />
              Create profile
            </button>
          </form>

          <div className={styles.tableWrap}>
            {users.length > 0 ? (
              <div className={styles.userList}>
                {users.map((user) => {
                  const draft = editingId === user.id ? editingDraft : null;
                  return (
                    <article key={user.id} className={styles.userCard}>
                      <div className={styles.userHeader}>
                        <div>
                          <strong>{user.username}</strong>
                          <span>{user.email}</span>
                        </div>
                        <div className={styles.badges}>
                          {user.roles.map((role) => (
                            <span key={role} className={styles.badge}>{role}</span>
                          ))}
                          <span className={`${styles.badge} ${user.enabled ? styles.enabled : styles.disabled}`}>{user.enabled ? 'Enabled' : 'Disabled'}</span>
                        </div>
                      </div>

                      {editingId === user.id && draft ? (
                        <form className={styles.editForm} onSubmit={(event) => handleUpdate(event, user.id)}>
                          <div className={styles.grid}>
                            <label className={styles.field}>
                              <span>Username</span>
                              <input value={draft.username} onChange={(event) => setDrafts({ ...drafts, [user.id]: { ...draft, username: event.target.value } })} required />
                            </label>
                            <label className={styles.field}>
                              <span>Email</span>
                              <input value={draft.email} onChange={(event) => setDrafts({ ...drafts, [user.id]: { ...draft, email: event.target.value } })} type="email" required />
                            </label>
                            <label className={styles.field}>
                              <span>Full name</span>
                              <input value={draft.fullName} onChange={(event) => setDrafts({ ...drafts, [user.id]: { ...draft, fullName: event.target.value } })} />
                            </label>
                            <label className={styles.field}>
                              <span>Roles</span>
                              <input value={draft.roles} onChange={(event) => setDrafts({ ...drafts, [user.id]: { ...draft, roles: event.target.value } })} />
                            </label>
                            <label className={styles.field}>
                              <span>Phone number</span>
                              <input value={draft.phoneNumber} onChange={(event) => setDrafts({ ...drafts, [user.id]: { ...draft, phoneNumber: event.target.value } })} />
                            </label>
                            <label className={styles.field}>
                              <span>Birthday</span>
                              <input value={draft.birthday} onChange={(event) => setDrafts({ ...drafts, [user.id]: { ...draft, birthday: event.target.value } })} type="date" />
                            </label>
                            <label className={styles.field}>
                              <span>Position</span>
                              <input value={draft.position} onChange={(event) => setDrafts({ ...drafts, [user.id]: { ...draft, position: event.target.value } })} />
                            </label>
                            <label className={styles.field}>
                              <span>Profile picture URL</span>
                              <input value={draft.profilePictureUrl} onChange={(event) => setDrafts({ ...drafts, [user.id]: { ...draft, profilePictureUrl: event.target.value } })} />
                            </label>
                            <label className={styles.fieldFull}>
                              <span>Social contacts JSON</span>
                              <textarea rows={4} value={draft.socialContactsJson} onChange={(event) => setDrafts({ ...drafts, [user.id]: { ...draft, socialContactsJson: event.target.value } })} />
                            </label>
                            <label className={styles.checkbox}>
                              <input
                                checked={draft.enabled}
                                onChange={(event) => setDrafts({ ...drafts, [user.id]: { ...draft, enabled: event.target.checked } })}
                                type="checkbox"
                              />
                              <span>Enabled</span>
                            </label>
                          </div>

                          <div className={styles.actions}>
                            <button className={styles.primaryAction} type="submit" disabled={busy}>
                              <Save size={18} />
                              Save changes
                            </button>
                            <button className={styles.secondaryAction} type="button" onClick={() => setEditingId(null)}>
                              Cancel
                            </button>
                          </div>
                        </form>
                      ) : (
                        <div className={styles.actions}>
                          <button className={styles.secondaryAction} type="button" onClick={() => beginEdit(user)}>
                            Edit profile
                          </button>
                        </div>
                      )}
                    </article>
                  );
                })}
              </div>
            ) : (
              <p className={styles.emptyState}>Load the directory to view and manage users.</p>
            )}
          </div>
        </>
      )}
    </section>
  );
}
