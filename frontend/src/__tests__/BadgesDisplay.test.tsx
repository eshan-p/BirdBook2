import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { BadgesDisplay } from '../components/common/BadgesDisplay';

describe('BadgesDisplay Component', () => {
  test('should display all four badges', () => {
    render(<BadgesDisplay unlockedBadges={[]} />);
    expect(screen.getByText('First Flight')).toBeInTheDocument();
    expect(screen.getByText('Collector')).toBeInTheDocument();
    expect(screen.getByText('Social Butterfly')).toBeInTheDocument();
    expect(screen.getByText('Photographer')).toBeInTheDocument();
  });

  test('should show unlocked status for unlocked badges', () => {
    render(
      <BadgesDisplay unlockedBadges={['first_sighting', 'collector']} />
    );
    const unlockedElements = screen.getAllByText('Unlocked');
    expect(unlockedElements.length).toBe(2);
  });

  test('should display locked badges with reduced opacity', () => {
    const { container } = render(<BadgesDisplay unlockedBadges={[]} />);
    const lockedBadges = container.querySelectorAll('.opacity-50');
    expect(lockedBadges.length).toBe(4);
  });
});