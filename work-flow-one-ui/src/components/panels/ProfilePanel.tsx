import { FormEvent, useEffect, useMemo, useState } from 'react';
import { KeyRound, Save } from 'lucide-react';
import { ProfilePayload, PasswordPayload } from '../../context/AuthContext';
import { UserResponse } from '../../types/UserResponse';
import { formatSocialContacts, parseSocialContacts } from '../../utils/socialContacts';
import styles from './ProfilePanel.module.css';

type ProfilePanelProps = {
  profile: UserResponse | null;
  busy: boolean;
  onSaveProfile: (payload: ProfilePayload) => Promise<void>;
  onChangePassword: (payload: PasswordPayload) => Promise<void>;
};

function toDateInput(value: string | null) {
  return value ? value.slice(0, 10) : '';
}

export function ProfilePanel({ profile, busy, onSaveProfile, onChangePassword }: ProfilePanelProps) {
  const [email, setEmail] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [birthday, setBirthday] = useState('');
  const [position, setPosition] = useState('');
  const [profilePictureUrl, setProfilePictureUrl] = useState('');
  const [socialContactsJson, setSocialContactsJson] = useState('{}');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');

  useEffect(() => {
    setEmail(profile?.email ?? '');
    setPhoneNumber(profile?.phoneNumber ?? '');
    setBirthday(toDateInput(profile?.birthday ?? null));
    setPosition(profile?.position ?? '');
    setProfilePictureUrl(profile?.profilePictureUrl ?? '');
    setSocialContactsJson(formatSocialContacts(profile?.socialContacts ?? {}));
  }, [profile]);

  const avatarLabel = useMemo(() => profile?.username?.slice(0, 1).toUpperCase() ?? 'U', [profile]);

  async function handleProfileSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      await onSaveProfile({
        email,
        phoneNumber,
        birthday,
        position,
        profilePictureUrl,
        socialContacts: parseSocialContacts(socialContactsJson),
      });
    } catch {
      return;
    }
  }

  async function handlePasswordSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    try {
      await onChangePassword({ currentPassword, newPassword });
      setCurrentPassword('');
      setNewPassword('');
    } catch {
      return;
    }
  }

  return (
    <section className={styles.panel} id="profile">
      <div className={styles.panelHeading}>
        <div>
          <p className={styles.eyebrow}>Profile</p>
          <h2>Current user</h2>
        </div>
        <div className={styles.avatarWrap}>
          {profilePictureUrl ? <img src={profilePictureUrl} alt="" /> : <span>{avatarLabel}</span>}
        </div>
      </div>

      {profile ? (
        <>
          <dl className={styles.summary}>
            <div>
              <dt>Username</dt>
              <dd>{profile.username}</dd>
            </div>
            <div>
              <dt>Roles</dt>
              <dd>{profile.roles.join(', ') || 'None'}</dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>{profile.enabled ? 'Enabled' : 'Disabled'}</dd>
            </div>
          </dl>

          <form className={styles.formGrid} onSubmit={handleProfileSubmit}>
            <label className={styles.field}>
              <span>Email</span>
              <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" autoComplete="email" />
            </label>
            <label className={styles.field}>
              <span>Phone number</span>
              <input value={phoneNumber} onChange={(event) => setPhoneNumber(event.target.value)} autoComplete="tel" />
            </label>
            <label className={styles.field}>
              <span>Birthday</span>
              <input value={birthday} onChange={(event) => setBirthday(event.target.value)} type="date" />
            </label>
            <label className={styles.field}>
              <span>Position</span>
              <input value={position} onChange={(event) => setPosition(event.target.value)} autoComplete="organization-title" />
            </label>
            <label className={styles.field}>
              <span>Profile picture URL</span>
              <input value={profilePictureUrl} onChange={(event) => setProfilePictureUrl(event.target.value)} autoComplete="url" />
            </label>
            <label className={styles.fieldFull}>
              <span>Social contacts JSON</span>
              <textarea rows={6} value={socialContactsJson} onChange={(event) => setSocialContactsJson(event.target.value)} />
            </label>
            <button className={styles.primaryAction} type="submit" disabled={busy}>
              <Save size={18} />
              Save profile
            </button>
          </form>

          <form className={styles.passwordForm} onSubmit={handlePasswordSubmit}>
            <div className={styles.subheading}>
              <KeyRound size={16} />
              <h3>Change password</h3>
            </div>
            <div className={styles.formGrid}>
              <label className={styles.field}>
                <span>Current password</span>
                <input value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} type="password" autoComplete="current-password" />
              </label>
              <label className={styles.field}>
                <span>New password</span>
                <input value={newPassword} onChange={(event) => setNewPassword(event.target.value)} type="password" autoComplete="new-password" />
              </label>
            </div>
            <button className={styles.secondaryAction} type="submit" disabled={busy || !currentPassword || !newPassword}>
              <Save size={18} />
              Update password
            </button>
          </form>
        </>
      ) : (
        <p className={styles.emptyState}>Sign in to edit your profile.</p>
      )}
    </section>
  );
}
